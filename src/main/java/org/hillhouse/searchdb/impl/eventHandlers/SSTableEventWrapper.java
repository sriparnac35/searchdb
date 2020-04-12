package org.hillhouse.searchdb.impl.eventHandlers;

import com.google.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hillhouse.searchdb.impl.datastores.SSTableDataStore;
import org.hillhouse.searchdb.interfaces.capabilities.Initializable;
import org.hillhouse.searchdb.interfaces.dao.IDDao;
import org.hillhouse.searchdb.interfaces.eventSystem.EventManager;
import org.hillhouse.searchdb.interfaces.eventSystem.EventPublisher;
import org.hillhouse.searchdb.interfaces.eventSystem.EventSubscriber;
import org.hillhouse.searchdb.models.diskDS.SSTableDataKey;
import org.hillhouse.searchdb.models.diskDS.SSTableDataValue;
import org.hillhouse.searchdb.models.diskDS.SSTableDataValueItem;
import org.hillhouse.searchdb.models.events.MemTableAvailableForSinkEvent;
import org.hillhouse.searchdb.models.events.PersistToSSTableBeginEvent;
import org.hillhouse.searchdb.models.events.PersistToSSTableEndEvent;
import org.hillhouse.searchdb.models.events.PersistToSSTableFailedEvent;
import org.hillhouse.searchdb.models.memory.Memtable;

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

    private Map<String, EventSubscriber> eventSubscribers;

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

    private void notifySSTableCreationStated(Memtable memTable) {
        PersistToSSTableBeginEvent event = PersistToSSTableBeginEvent.builder().walID(memTable.getWalID())
                .beginLogID(memTable.getBeginLogID()).endLogID(memTable.getEndLogID()).build();
        eventManager.publishEvent(this, event);
    }

    private void notifyNewSSTableCreated(Memtable memTable, String ssTable) {
        PersistToSSTableEndEvent persistToSSTableEndEvent = PersistToSSTableEndEvent.builder()
                .ssTableName(ssTable).walID(memTable.getWalID())
                .beginLogID(memTable.getBeginLogID()).endLogID(memTable.getEndLogID()).build();
        eventManager.publishEvent(this, persistToSSTableEndEvent);
    }

    private void notifySSTableCreationFailed(Memtable memTable) {
        PersistToSSTableFailedEvent event = PersistToSSTableFailedEvent.builder().walID(memTable.getWalID())
                .beginLogID(memTable.getBeginLogID()).endLogID(memTable.getEndLogID()).build();
        eventManager.publishEvent(this, event);
    }


    private void registerEventHandlers() throws Exception {
        for (Map.Entry<String, EventSubscriber> subscriber : eventSubscribers.entrySet()) {
            ((Initializable) subscriber.getValue()).initialize();
            eventManager.subscribeToEvent(subscriber.getValue(), subscriber.getKey());
        }
    }

    private void unregisterEventHandlers() throws Exception {
        for (Map.Entry<String, EventSubscriber> subscriber : eventSubscribers.entrySet()) {
            eventManager.subscribeToEvent(subscriber.getValue(), subscriber.getKey());
            ((Initializable) subscriber.getValue()).destroy();
        }
        eventSubscribers.clear();
    }

    private class MemtableFullEventHandler implements Initializable, EventSubscriber<MemTableAvailableForSinkEvent> {
        @Inject
        private ExecutorService executorService;

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
    private class SSTableCreateRunnable implements Runnable {
        private Memtable memTable;

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

        private SSTableDataKey createKey() {
            return SSTableDataKey.builder().walID(memTable.getWalID())
                    .tableIdentifier(String.valueOf(idDao.getNextID())).startID(memTable.getBeginLogID())
                    .endID(memTable.getEndLogID()).build();
        }

        private SSTableDataValue createValue() {
            List<SSTableDataValueItem> dataValueItems = memTable.readAll().stream().map(this::map).collect(Collectors.toList());
            return SSTableDataValue.builder().dataValueItems(dataValueItems).build();
        }

        private SSTableDataValueItem map(Memtable.DataItem data) {
            return SSTableDataValueItem.builder().rowKey(data.getRowID()).isDeleted(data.isDeleted())
                    .value(data.getValue()).build();
        }
    }

}
