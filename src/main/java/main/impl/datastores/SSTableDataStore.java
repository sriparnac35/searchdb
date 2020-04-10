package main.impl.datastores;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import main.constants.SSTableConstants;
import main.interfaces.dao.DiskDao;
import main.interfaces.processors.DataStore;
import main.models.diskDS.*;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class SSTableDataStore implements DataStore<SSTableDataKey, SSTableDataValue, SSTableDataKey, SSTableDataValue> {
    private static final String FILE_PREFIX = "ss_";
    private final DiskDao diskDao;
    private final boolean shouldIndex;


    @Override
    public void insert(SSTableDataKey key, SSTableDataValue value) throws IOException{
        createNewSSTable(key, value);
    }

    @Override
    public void update(SSTableDataKey key, SSTableDataValue value) throws IOException{
        throw new UnsupportedOperationException("update not supported in SSTables");
    }

    @Override
    public void delete(SSTableDataKey key) throws IOException {
        diskDao.deleteWithName(FILE_PREFIX + key.getTableIdentifier());
    }

    @Override
    public void initialize() throws Exception {
        diskDao.initialize();
    }

    @Override
    public void destroy() throws Exception {
        diskDao.destroy();
    }

    @Override
    public SSTableDataValue search(SSTableDataKey key) throws IOException{
        String ssTableName = deriveSSTableNameForKey(key);
        byte[] bytes = diskDao.getData(ssTableName, key.getStartID(), key.getEndID());
        return new SSTableDataValue(extractValueItemFromOffset(bytes));
    }

    private List<SSTableDataValueItem> extractValueItemFromOffset(byte[] bytes){
        List<SSTableDataValueItem> result = new ArrayList<>();
        int currentIndex = 0;
        while(currentIndex < bytes.length - 1){
            String rowKey = new String(Arrays.copyOfRange(bytes, currentIndex + SSTableConstants.OFFSET_ROW_ID, SSTableConstants.MAX_ROW_ID_LENGTH));
            byte flag = bytes[currentIndex + SSTableConstants.OFFSET_ROW_FLAG];
            int length = new BigInteger(Arrays.copyOfRange(bytes,currentIndex + SSTableConstants.OFFSET_ROW_VALUE_LENGTH, SSTableConstants.MAX_ROW_ID_LENGTH)).intValue();
            if (flag != SSTableConstants.FLAG_DELETED){
                String value = new String(Arrays.copyOfRange(bytes, currentIndex + SSTableConstants.OFFSET_ROW_VALUE_DATA, length));
                SSTableDataValueItem v = SSTableDataValueItem.builder().rowKey(rowKey).value(value).build();
                result.add(v);
            }
            currentIndex += SSTableConstants.OFFSET_ROW_VALUE_DATA + length;
        }
        return result;
    }

    private void createNewSSTable(SSTableDataKey key, SSTableDataValue value) throws IOException{
        createNewSSTableForKey(key);
        List<RowObject> data = value.getDataValueItems().stream().map(this::createRowPayload).collect(Collectors.toList());
        byte[] wrappedRowData = wrapRowData(data);
        diskDao.writeAndSyncToLatest(wrappedRowData);
    }

    private byte[] wrapRowData(List<RowObject> data){
        int totalSize = deriveSizeForSSTable(data);
        ByteBuffer byteBuffer = ByteBuffer.allocate(totalSize);
        byteBuffer.put(SSTableConstants.START_BYTE);
        data.forEach(item -> byteBuffer.put(item.data));
        byteBuffer.put(SSTableConstants.END_BYTE);
        return byteBuffer.array();
    }

    private int deriveSizeForSSTable(List<RowObject> data){
        int dataSize = data.stream().map(item -> item.length).reduce(0, Integer::sum);
        return SSTableConstants.HEADER_SIZE_BYTES + SSTableConstants.TAIL_SIZE_BYTES + dataSize;
    }

    private void createNewSSTableForKey(SSTableDataKey key) throws IOException{
        String fileName = deriveSSTableNameForKey(key);
        diskDao.createWithName(fileName);
        diskDao.makeCurrent(fileName);
    }

    private String deriveSSTableNameForKey(SSTableDataKey key){
        return FILE_PREFIX + key.getTableIdentifier();
    }

    private RowObject createRowPayload(SSTableDataValueItem dataItem){
        String rowKey = dataItem.getRowKey();
        byte[] dataAsBytes = dataItem.isDeleted() ? new byte[0] : dataItem.getValue().getBytes();
        byte[] finalData = new byte[SSTableConstants.ROW_HEADER_SIZE_BYTES + dataAsBytes.length];

        System.arraycopy(rowKey.getBytes(), 0, finalData, SSTableConstants.ROW_HEADER_SIZE_BYTES - rowKey.length(), rowKey.length());
        finalData[SSTableConstants.OFFSET_ROW_FLAG] = dataItem.isDeleted() ? SSTableConstants.FLAG_DELETED : SSTableConstants.FLAG_UPDATED;
        System.arraycopy(BigInteger.valueOf(dataAsBytes.length).toByteArray(), 0, finalData, SSTableConstants.OFFSET_ROW_VALUE_LENGTH, SSTableConstants.SIZE_BYTE_VALUE_LENGTH);
        System.arraycopy(dataAsBytes, 0, finalData, SSTableConstants.OFFSET_ROW_VALUE_DATA, dataAsBytes.length);
        return new RowObject(finalData, finalData.length);
    }

    @AllArgsConstructor
    private static final class RowObject{
        byte[] data ;
        int length;
    }
}