package org.hillhouse.searchdb.impl.eventHandlers;

import com.google.inject.Inject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hillhouse.searchdb.impl.datastores.MemTableDataStore;
import org.hillhouse.searchdb.interfaces.capabilities.Initializable;
import org.hillhouse.searchdb.interfaces.eventSystem.EventManager;
import org.hillhouse.searchdb.interfaces.eventSystem.EventSubscriber;
import org.hillhouse.searchdb.interfaces.utilities.DocumentQueue;
import org.hillhouse.searchdb.models.memory.MemTableDataKey;
import org.hillhouse.searchdb.models.memory.MemTableDataValue;
import org.hillhouse.searchdb.models.wal.entries.WalDataEntry;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.hillhouse.searchdb.constants.MemtableConstants.MEMTABLE_SOURCE_INTERVAL_IN_SEC;

@Slf4j
public class MemtableSourceWrapper implements Initializable {
    @Inject private DocumentQueue<WalDataEntry> documentQueue;
    @Inject private MemTableDataStore dataStore;
    private ScheduledExecutorService executorService;

    @Override
    public void initialize() throws Exception {
        dataStore.initialize();
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleWithFixedDelay(new WalEntryRunnable(), 0, MEMTABLE_SOURCE_INTERVAL_IN_SEC, TimeUnit.SECONDS);
    }

    @Override
    public void destroy() throws Exception {
       executorService.shutdown();
    }

    private class WalEntryRunnable implements Runnable{

        @Override
        public void run() {
            try{
                WalDataEntry walQueueItem = documentQueue.getNext();
                MemTableDataKey dataKey = MemTableDataKey.builder().walID(walQueueItem.getWalID())
                        .logID(walQueueItem.getLogID()).rowKey(walQueueItem.getRowKey()).build();
                switch (walQueueItem.getOperationType()) {
                    case INSERT:
                        dataStore.insert(dataKey, MemTableDataValue.builder().value(walQueueItem.getValue()).build());break;
                    case UPDATE:
                        dataStore.update(dataKey, MemTableDataValue.builder().value(walQueueItem.getValue()).build());break;
                    case DELETE:
                        dataStore.delete(dataKey);break;
                }
                documentQueue.ack();
            }catch (Exception e){
                log.error(e.getMessage(), e);
            }

        }
    }
}
