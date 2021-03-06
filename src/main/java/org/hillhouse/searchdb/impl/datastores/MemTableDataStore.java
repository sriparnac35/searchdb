package org.hillhouse.searchdb.impl.datastores;

import com.google.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.hillhouse.searchdb.constants.MemtableConstants;
import org.hillhouse.searchdb.interfaces.eventSystem.EventManager;
import org.hillhouse.searchdb.interfaces.eventSystem.EventPublisher;
import org.hillhouse.searchdb.interfaces.processors.DataStore;
import org.hillhouse.searchdb.models.memory.*;
import org.hillhouse.searchdb.models.wrappers.CurrentMemtableWrapper;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@RequiredArgsConstructor
public class MemTableDataStore implements EventPublisher, DataStore<MemTableDataKey, MemTableDataValue, MemTableDataKey, MemtableSearchValue> {
    private final CurrentMemtableWrapper memtableWrapper;
    private ExecutorService executorService;

    @Override
    public synchronized void initialize() throws Exception {
        if (executorService == null){
            executorService = Executors.newSingleThreadExecutor();
            createNewMemTable();
        }
    }

    @Override
    public void destroy() throws Exception {
        executorService.shutdown();
    }

    @Override
    public void insert(MemTableDataKey key, MemTableDataValue value) throws IOException {
        checkAndHandleIfMemtableFull();
        executorService.submit(new InsertDataRunnable(key, value));
    }

    @Override
    public void update(MemTableDataKey key, MemTableDataValue value) throws IOException {
        checkAndHandleIfMemtableFull();
        executorService.submit(new InsertDataRunnable(key, value));
    }

    @Override
    public void delete(MemTableDataKey key) throws IOException {
        checkAndHandleIfMemtableFull();
        executorService.submit(new InsertDataRunnable(key, MemTableDataValue.builder().isDeleted(true).build()));
    }

    @Override
    public MemtableSearchValue search(MemTableDataKey key) throws IOException {
        Memtable.DataItem dataItem = search(key.getRowKey());
        List<MemtableSearchValueItem> values = (dataItem == null) ? null : Collections.singletonList(MemtableSearchValueItem.builder()
                .isDeleted(dataItem.isDeleted()).value(dataItem.getValue()).build());
        return new MemtableSearchValue(values);
    }

    @Override
    public String getPublisherID() {
        return this.getClass().getSimpleName();
    }

    private void checkAndHandleIfMemtableFull() {
        if (isMemtableFull()) {
            executorService.submit(new MemtableFullHandlerRunnable());
        }
    }

    private void createNewMemTable() {
        memtableWrapper.createNewMemtable();
    }

    private boolean isMemtableFull() {
        return memtableWrapper.getCurrentMemtable().size() == MemtableConstants.MAX_SIZE;
    }

    private Memtable.DataItem search(String key) throws IOException {
        Memtable.DataItem dataItem = memtableWrapper.getCurrentMemtable().search(key);
        if (dataItem != null){
            return dataItem;
        }
        for(Iterator<Memtable> iterator = memtableWrapper.getOldTables().iterator() ; iterator.hasNext(); ){
            dataItem = iterator.next().search(key);
            if (dataItem != null){
                break;
            }
        }
        return dataItem;
    }

    @AllArgsConstructor
    private class InsertDataRunnable implements Runnable {
        private MemTableDataKey dataKey;
        private MemTableDataValue dataValue;

        @Override
        public void run() {
            System.out.println("inserting : " + dataKey.getRowKey());
            Memtable.DataItem dataItem = Memtable.DataItem.builder().rowID(dataKey.getRowKey())
                    .value(dataValue.getValue()).isDeleted(dataValue.isDeleted()).build();
            Memtable currentMemTable = memtableWrapper.getCurrentMemtable();
            if (currentMemTable.getWalID() == null) {
                currentMemTable.setWalID(dataKey.getWalID());
                currentMemTable.setBeginLogID(dataKey.getLogID());
            }
            currentMemTable.setEndLogID(dataKey.getLogID());
            currentMemTable.insert(dataItem);
        }
    }

    private class MemtableFullHandlerRunnable implements Runnable {
        @Override
        public void run() {
            memtableWrapper.createNewMemtable();
        }
    }


}
