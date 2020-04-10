package main.impl.eventHandlers;

import com.google.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.impl.datastores.SSTableDataStore;
import main.interfaces.capabilities.Initializable;
import main.interfaces.dao.IDDao;
import main.interfaces.eventSystem.EventManager;
import main.interfaces.eventSystem.EventPublisher;
import main.interfaces.eventSystem.EventSubscriber;
import main.models.diskDS.SSTableDataKey;
import main.models.diskDS.SSTableDataValue;
import main.models.diskDS.SSTableDataValueItem;
import main.models.events.*;
import main.impl.datastores.MemTableDataStore;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@NoArgsConstructor
@Slf4j
public class SSTableEventWrapper implements Initializable, EventPublisher {
    @Inject private EventManager eventManager;
    @Inject private SSTableDataStore dataStore;
    @Inject private IDDao idDao;

    private Map<String, EventSubscriber> eventSubscribers ;

    {
        eventSubscribers = new HashMap<>();
        MemtableFullEventHandler memtableFullEventHandler = new MemtableFullEventHandler();
        eventSubscribers.put(MemTableAvailableForSinkEvent.class.getSimpleName(), memtableFullEventHandler);
    }

    @Override
    public void initialize() throws Exception {
        registerEventHandlers();
    }

    @Override
    public void destroy() throws Exception {
        unregisterEventHandlers();
    }

    @Override
    public String getPublisherID() {
        return this.getClass().getSimpleName();
    }

    private void notifySSTableCreationStated(MemTableDataStore memTable){
        PersistToSSTableBeginEvent event = PersistToSSTableBeginEvent.builder().walID(memTable.getWalID())
                .beginLogID(memTable.getBeginLogID().get()).endLogID(memTable.getEndLogID().get()).build();
        eventManager.publishEvent(this, event);
    }

    private void notifyNewSSTableCreated(MemTableDataStore memTable, String ssTable){
        PersistToSSTableEndEvent persistToSSTableEndEvent = PersistToSSTableEndEvent.builder()
                .ssTableName(ssTable).walID(memTable.getWalID())
                .beginLogID(memTable.getBeginLogID()).endLogID(memTable.getEndLogID()).build();
        eventManager.publishEvent(this, persistToSSTableEndEvent);
    }

    private void notifySSTableCreationFailed(MemTableDataStore memTable){
        PersistToSSTableFailedEvent event = PersistToSSTableFailedEvent.builder().walID(memTable.getWalID())
                .beginLogID(memTable.getBeginLogID().get()).endLogID(memTable.getEndLogID().get()).build();
        eventManager.publishEvent(this, event);
    }


    private void registerEventHandlers() throws Exception {
        for (Map.Entry<String, EventSubscriber> subscriber : eventSubscribers.entrySet()){
            ((Initializable)subscriber.getValue()).initialize();
            eventManager.subscribeToEvent(subscriber.getValue(), subscriber.getKey());
        }
    }

    private void unregisterEventHandlers() throws Exception{
        for (Map.Entry<String, EventSubscriber> subscriber : eventSubscribers.entrySet()){
            eventManager.subscribeToEvent(subscriber.getValue(), subscriber.getKey());
            ((Initializable)subscriber.getValue()).destroy();
        }
        eventSubscribers.clear();
    }

    private class MemtableFullEventHandler implements Initializable, EventSubscriber<MemTableAvailableForSinkEvent>{
        @Inject private ExecutorService executorService;

        @Override
        public void onEvent(MemTableAvailableForSinkEvent event) {
            executorService.submit(new SSTableCreateRunnable(event.getMemTable()));
        }

        @Override
        public void initialize() throws Exception {
            executorService = Executors.newSingleThreadScheduledExecutor();
        }

        @Override
        public void destroy() throws Exception {
            executorService.shutdown();
        }
    }

    @AllArgsConstructor
    private class SSTableCreateRunnable implements Runnable{
        private MemTableDataStore memTable;

        @Override
        public void run() {
            notifySSTableCreationStated(memTable);
            try {
                SSTableDataKey key = createKey();
                dataStore.insert(key, createValue());
                notifyNewSSTableCreated(memTable, key.getTableIdentifier());
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                notifySSTableCreationFailed(memTable);
            }
        }

        private SSTableDataKey createKey(){
            return SSTableDataKey.builder().walID(memTable.getWalID())
                    .tableIdentifier(String.valueOf(idDao.getNextID())).startID(memTable.getBeginLogID().get())
                    .endID(memTable.getEndLogID().get()).build();
        }

        private SSTableDataValue createValue(){
            List<SSTableDataValueItem> dataValueItems = memTable.getDataItemList().stream().map(this::map).collect(Collectors.toList());
            return SSTableDataValue.builder().dataValueItems(dataValueItems).build();
        }

        private SSTableDataValueItem map(MemTableDataStore.DataItem data){
            return SSTableDataValueItem.builder().rowKey(data.getRowID()).isDeleted(data.isDeleted())
                    .value(data.getValue()).build();
        }
    }

}
