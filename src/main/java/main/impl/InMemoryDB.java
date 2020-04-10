package main.impl;

import com.google.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.constants.Constants;
import main.interfaces.capabilities.Initializable;
import main.interfaces.eventSystem.EventManager;
import main.interfaces.eventSystem.EventSubscriber;
import main.interfaces.utilities.DocumentQueue;
import main.models.events.DocumentWaldEvent;
import main.models.events.Event;
import main.models.events.PersistToSSTableEndEvent;
import main.impl.datastores.MemTableDataStore;
import main.models.wal.WALQueueItem;

import java.util.ArrayDeque;
import java.util.Queue;

@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class InMemoryDB implements Initializable, EventSubscriber {
    @Inject private EventManager eventManager;
    @Inject private DocumentQueue<WALQueueItem> documentQueue;
    private MemTableDataStore memTable;
    private Queue<MemTableDataStore> oldMemTables ;

    @Override
    public void initialize() throws Exception {
        memTable = new MemTableDataStore();
        memTable.initialize();
        oldMemTables = new ArrayDeque<>();
        eventManager.subscribeToEvent(this, DocumentWaldEvent.class.getSimpleName());
        eventManager.subscribeToEvent(this, PersistToSSTableEndEvent.class.getSimpleName());
    }

    @Override
    public void destroy() throws Exception {
        memTable.destroy();
        eventManager.unsubscribeToEvent(this, DocumentWaldEvent.class.getSimpleName());
        eventManager.unsubscribeToEvent(this, PersistToSSTableEndEvent.class.getSimpleName());
    }

    @Override
    public void onEvent(Event event) {
        switch (event.getName()){
            case "DocumentWaldEvent":
                onDocumentWald((DocumentWaldEvent) event);
                break;

            case "PersistToSSTableEndEvent":
                onDocumentsPersisted((PersistToSSTableEndEvent) event);
                break;

        }
    }

    private void onDocumentWald(DocumentWaldEvent event){
        try{
            WALQueueItem walQueueItem = documentQueue.getNext();
            updateMemtableMeta(walQueueItem);
            performWriteOperation(walQueueItem);
            if (isTableFull()){
                processMemTable();
            }
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
    }

    private void updateMemtableMeta(WALQueueItem walQueueItem ){
        if(memTable.getBeginLogID() == null){
            memTable.setWalID(walQueueItem.getWalID());
            memTable.setBeginLogID(walQueueItem.getLogID());
        }
        memTable.setEndLogID(walQueueItem.getLogID());
    }

    private void performWriteOperation(WALQueueItem walQueueItem){
        switch (walQueueItem.getOperationType()){
            case INSERT:
                memTable.insert(walQueueItem.getRowKey(), walQueueItem.getValue()); break;
            case UPDATE:
                memTable.update(walQueueItem.getRowKey(), walQueueItem.getValue()); break;
            case DELETE:
                memTable.delete(walQueueItem.getRowKey()); break;
        }
    }

    private void processMemTable() throws Exception {
        oldMemTables.offer(memTable);
        memTable = new MemTableDataStore();
        memTable.initialize();
    }

    private boolean isTableFull(){
        return memTable.count() == Constants.MAX_IN_MEMORY_SIZE;
    }

    private void onDocumentsPersisted(PersistToSSTableEndEvent event){
        // TODO: add validations
        oldMemTables.poll();
    }
}
