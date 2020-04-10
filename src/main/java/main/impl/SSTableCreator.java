package main.impl;

import com.google.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.interfaces.capabilities.Initializable;
import main.interfaces.dao.DiskDao;
import main.interfaces.dao.IDDao;
import main.interfaces.eventSystem.EventManager;
import main.interfaces.eventSystem.EventPublisher;
import main.interfaces.eventSystem.EventSubscriber;
import main.models.diskDS.DiskDataKey;
import main.models.diskDS.DiskDataValue;
import main.models.diskDS.SSTableConstants;
import main.models.events.MemTableAvailableForSinkEvent;
import main.models.events.NewIndexAvailableEvent;
import main.models.events.PersistToSSTableBeginEvent;
import main.models.events.PersistToSSTableEndEvent;
import main.models.memory.Index;
import main.impl.datastores.MemTableDataStore;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class SSTableCreator implements EventSubscriber<MemTableAvailableForSinkEvent>, Initializable, EventPublisher, DataReader<DiskDataKey, List<DiskDataValue>> {
    private static final String SSTABLE_PREFIX = "ss_";
    private static final String SSTABLE_DIR = "./";
    private static final int INDEX_GAP = 4;

   @Inject private DiskDao diskDao;
   @Inject private IDDao idDao;
   @Inject private EventManager eventManager;
   private ExecutorService executorService;

    @Override
    public void onEvent(MemTableAvailableForSinkEvent event) {
        executorService.submit(new EventHandlerRunnable(event));
    }

    @Override
    public void initialize() throws Exception {
        executorService = Executors.newFixedThreadPool(1);
        diskDao.initialize();
        eventManager.subscribeToEvent(this, MemTableAvailableForSinkEvent.class.getSimpleName());
    }

    @Override
    public void destroy() throws Exception {
        diskDao.destroy();
        eventManager.unsubscribeToEvent(this, MemTableAvailableForSinkEvent.class.getSimpleName());
        executorService.shutdown();
    }

    @Override
    public String getPublisherID() {
        return this.getClass().getSimpleName();
    }

    @Override
    public List<DiskDataValue> read(DiskDataKey key) {
        List<DiskDataValue> result = new ArrayList<>();
        try{
            byte[] bytes = diskDao.getData(key.getSstableName(), key.getStartOffset(), key.getEndOffset());
            int currentIndex = 0;

            while(currentIndex < bytes.length - 1){
                String rowKey = new String(Arrays.copyOfRange(bytes, currentIndex + SSTableConstants.OFFSET_ROW_ID, SSTableConstants.MAX_ROW_ID_LENGTH));
                byte flag = bytes[currentIndex + SSTableConstants.OFFSET_ROW_FLAG];
                int length = new BigInteger(Arrays.copyOfRange(bytes,currentIndex + SSTableConstants.OFFSET_ROW_VALUE_LENGTH, SSTableConstants.MAX_ROW_ID_LENGTH)).intValue();
                if (flag != SSTableConstants.FLAG_DELETED){
                    String value = new String(Arrays.copyOfRange(bytes, currentIndex + SSTableConstants.OFFSET_ROW_VALUE_DATA, length));
                    DiskDataValue v = DiskDataValue.builder().rowKey(rowKey).value(value).build();
                    result.add(v);
                }
                currentIndex += SSTableConstants.OFFSET_ROW_VALUE_DATA + length;
            }

        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return result;
    }

    private void onSSTableCreationStated(MemTableDataStore memTable){
        notifySSTableCreationStated(memTable);
    }

    private void onSSTableCreationEnded(MemTableDataStore memTable, String ssTable, Index index){
        notifyNewSSTableCreated(memTable, ssTable);
        notifyNewIndexAvailable(ssTable, index);
    }

    private void notifySSTableCreationStated(MemTableDataStore memTable){
        PersistToSSTableBeginEvent event = PersistToSSTableBeginEvent.builder().walID(memTable.getWalID())
                .beginLogID(memTable.getBeginLogID()).endLogID(memTable.getEndLogID()).build();
        eventManager.publishEvent(this, event);
    }

    private void notifyNewIndexAvailable(String ssTable, Index index){
        NewIndexAvailableEvent newIndexAvailableEvent = NewIndexAvailableEvent.builder()
                .ssTableName(ssTable).index(index).build();
        eventManager.publishEvent(this, newIndexAvailableEvent);
    }

    private void notifyNewSSTableCreated(MemTableDataStore memTable, String ssTable){
        PersistToSSTableEndEvent persistToSSTableEndEvent = PersistToSSTableEndEvent.builder()
                .ssTableName(ssTable).walID(memTable.getWalID())
                .beginLogID(memTable.getBeginLogID()).endLogID(memTable.getEndLogID()).build();
        eventManager.publishEvent(this, persistToSSTableEndEvent);
    }

    @RequiredArgsConstructor
    private final class EventHandlerRunnable implements Runnable{
        private final MemTableAvailableForSinkEvent event;

        @Override
        public void run() {
            DataSinkHandler dataSinkHandler = new DataSinkHandler(event);
            dataSinkHandler.handle();
        }
    }

    @AllArgsConstructor
    private static final class RowObject{
        byte[] data ;
        int length;
        int offset;
    }

    private class DataSinkHandler{
        private MemTableDataStore memTable;
        private String currentSSTableName;
        private Index index;

        public DataSinkHandler(MemTableAvailableForSinkEvent event){
            this.memTable = event.getMemTable();
        }

        public void handle(){
            try {
                onSSTableCreationStated(memTable);
                setup();
                List<MemTableDataStore.DataItem> dataItems = memTable.getDataItemList();
                List<RowObject> data = new ArrayList<>();
                int currentOffset = 0;
                int currentSize = 2;
                for (int i = 0; i < dataItems.size(); i++){
                    MemTableDataStore.DataItem dataItem = dataItems.get(i);
                    RowObject rowObject = createRowPayload(dataItem, currentOffset);
                    data.add(rowObject);
                    currentOffset += rowObject.length;
                    currentSize += rowObject.length;
                    if (shouldAddToIndex(i, dataItem)){
                        addToIndex(dataItem, rowObject);
                    }
                }
                ByteBuffer byteBuffer = ByteBuffer.allocate(currentSize);
                byteBuffer.put(SSTableConstants.START_BYTE);
                data.forEach(item -> byteBuffer.put(item.data));
                byteBuffer.put(SSTableConstants.END_BYTE);
                diskDao.writeAndSyncToLatest(byteBuffer.array());
                onSSTableCreationEnded(memTable, currentSSTableName, index);
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }

        private RowObject createRowPayload(MemTableDataStore.DataItem dataItem, int currentOffset){
            byte[] dataAsBytes = dataItem.isDeleted() ? new byte[0] : dataItem.getValue().getBytes();
            int totalItemSize = SSTableConstants.ROW_HEADER_SIZE_BYTES + dataAsBytes.length;
            byte[] finalData = new byte[totalItemSize + dataAsBytes.length];
            String rowKey = dataItem.getRowID();
            System.arraycopy(rowKey.getBytes(), 0, finalData,
                    SSTableConstants.ROW_HEADER_SIZE_BYTES - rowKey.length(), rowKey.length());

            finalData[SSTableConstants.OFFSET_ROW_FLAG] = dataItem.isDeleted() ?
                    SSTableConstants.FLAG_DELETED : SSTableConstants.FLAG_UPDATED;
            System.arraycopy(BigInteger.valueOf(dataAsBytes.length).toByteArray(), 0, finalData,
                    SSTableConstants.OFFSET_ROW_VALUE_LENGTH, SSTableConstants.SIZE_BYTE_VALUE_LENGTH);
            System.arraycopy(dataAsBytes, 0, finalData, SSTableConstants.OFFSET_ROW_VALUE_DATA,
                    dataAsBytes.length);
            return new RowObject(finalData, finalData.length, currentOffset);
        }

        private boolean shouldAddToIndex(int index, MemTableDataStore.DataItem dataItem){
            return index % INDEX_GAP == 0;
        }

        private void addToIndex(MemTableDataStore.DataItem dataItem, RowObject rowObject){
            Index.IndexItem indexItem = Index.IndexItem.builder().rowKey(dataItem.getRowID())
                    .offset(rowObject.offset + SSTableConstants.OFFSET_DATA_START_BYTE).build();
            index.addToIndex(indexItem);
        }

        private void setup() throws IOException{
            createSSTable();
            createIndex();
        }

        private void createIndex(){
            this.index = new Index(currentSSTableName);
        }

        private void createSSTable() throws IOException {
            this.currentSSTableName = getNextSSTableName();
            diskDao.createWithName(this.currentSSTableName);
        }

        private String getNextSSTableName(){
            return SSTABLE_DIR + SSTABLE_PREFIX + idDao.getNextID();
        }

    }
}
