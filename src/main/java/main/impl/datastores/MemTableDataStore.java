package main.impl.datastores;

import com.google.inject.Inject;
import lombok.*;
import main.interfaces.dao.IDDao;
import main.interfaces.eventSystem.EventManager;
import main.interfaces.eventSystem.EventPublisher;
import main.interfaces.eventSystem.EventSubscriber;
import main.interfaces.processors.DataStore;
import main.models.diskDS.SSTable;
import main.models.events.MemTableAvailableForSinkEvent;
import main.models.events.PersistToSSTableBeginEvent;
import main.models.events.PersistToSSTableEndEvent;
import main.models.events.PersistToSSTableFailedEvent;
import main.models.memory.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static main.models.memory.MemtableConstants.MEMTABLE_SINK_INTERVAL_IN_SEC;


public class MemTableDataStore implements EventPublisher, DataStore<MemTableDataKey, MemTableDataValue, MemTableDataKey, MemtableSearchValue> {
    @Inject private EventManager eventManager;
    @Inject private IDDao idDao;

    private Deque<Memtable> oldTables;
    private Memtable currentMemTable;
    private ExecutorService executorService;
    private ScheduledExecutorService sinkExecutorService;
    private boolean isSinkInProgress = false;


    public MemTableDataStore(EventManager eventManager){
        this.eventManager = eventManager;
    }

    @Override
    public void initialize() throws Exception {
        oldTables = new ArrayDeque<>();
        executorService = Executors.newSingleThreadExecutor();
        sinkExecutorService = Executors.newSingleThreadScheduledExecutor();
        createNewMemTable();
        sinkExecutorService.schedule(new PublishMemtableForSinkRunnable(), MEMTABLE_SINK_INTERVAL_IN_SEC, TimeUnit.SECONDS);
    }

    @Override
    public void destroy() throws Exception {
        oldTables.clear();
        executorService.shutdown();
        sinkExecutorService.shutdown();
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
        Memtable.DataItem dataItem = currentMemTable.search(key.getRowKey());
        MemtableSearchValueItem value = MemtableSearchValueItem.builder().isDeleted(dataItem.isDeleted()).value(dataItem.getValue()).build();
        return new MemtableSearchValue(Collections.singletonList(value));
    }

    @Override
    public String getPublisherID() {
        return this.getClass().getSimpleName();
    }

    private void checkAndHandleIfMemtableFull(){
        if (isMemtableFull()){
            executorService.submit(new MemtableFullHandlerRunnable());
        }
    }

    private void createNewMemTable(){
        this.currentMemTable = new Memtable(idDao.getNextID());
    }

    private boolean isMemtableFull(){
        return currentMemTable.size() == MemtableConstants.MAX_SIZE;
    }

    @AllArgsConstructor
    private class InsertDataRunnable implements Runnable{
        private MemTableDataKey dataKey;
        private MemTableDataValue dataValue;

        @Override
        public void run() {
            Memtable.DataItem dataItem = Memtable.DataItem.builder().rowID(dataKey.getRowKey())
                    .value(dataValue.getValue()).isDeleted(dataValue.isDeleted()).build();
            if (currentMemTable.getWalID() == null){
                currentMemTable.setWalID(dataKey.getWalID());
                currentMemTable.setBeginLogID(dataKey.getLogID());
            }
            currentMemTable.setEndLogID(dataKey.getLogID());
            currentMemTable.insert(dataItem);
        }
    }

    private class PublishMemtableForSinkRunnable implements Runnable{
        @Override
        public void run() {
            if (!isSinkInProgress){
                Memtable memtable = oldTables.peek();
                if (memtable != null){
                    isSinkInProgress = true;
                    notifyMemtableAvailableForSink(memtable);
                }
            }
        }

        private void notifyMemtableAvailableForSink(Memtable memtable){
            MemTableAvailableForSinkEvent event = MemTableAvailableForSinkEvent.builder().memTable(memtable).build();
            MemTableDataStore.this.eventManager.publishEvent(MemTableDataStore.this, event);
        }
    }

    @AllArgsConstructor
    private class MemtableSinkSuccessRunnable implements Runnable{
        private PersistToSSTableEndEvent event;

        @Override
        public void run() {
            oldTables.pop();
            isSinkInProgress = false;
        }
    }

    @AllArgsConstructor
    private class MemtableSinkFailedRunnable implements Runnable{
        private PersistToSSTableFailedEvent event;

        @Override
        public void run() {
            isSinkInProgress = false;
        }
    }


    public class MemtableSinkedSuccessEventHandler implements EventSubscriber<PersistToSSTableEndEvent>{
        @Override
        public void onEvent(PersistToSSTableEndEvent event) {
            sinkExecutorService.submit(new MemtableSinkSuccessRunnable(event));
        }
    }

    public class MemtableSinkedFailedEventHandler implements EventSubscriber<PersistToSSTableFailedEvent>{

        @Override
        public void onEvent(PersistToSSTableFailedEvent event) {
            sinkExecutorService.submit(new MemtableSinkFailedRunnable(event));
        }
    }

    private class MemtableFullHandlerRunnable implements Runnable{
        @Override
        public void run() {
            oldTables.addLast(currentMemTable);
            createNewMemTable();
        }
    }

}
