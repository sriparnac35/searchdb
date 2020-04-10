package main.impl.eventHandlers;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import main.impl.datastores.MemTableDataStore;
import main.interfaces.capabilities.Initializable;
import main.interfaces.eventSystem.EventManager;
import main.interfaces.eventSystem.EventSubscriber;
import main.interfaces.utilities.DocumentQueue;
import main.models.events.DocumentWaldEvent;
import main.models.memory.MemTableDataKey;
import main.models.memory.MemTableDataValue;
import main.models.wal.WALQueueItem;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class MemtableSourceWrapper implements Initializable {
    @Inject private EventManager eventManager;
    @Inject private DocumentQueue<WALQueueItem> documentQueue;
    @Inject private MemTableDataStore dataStore;

    private Map<String, EventSubscriber> eventSubscribers ;

    {
        eventSubscribers = new HashMap<>();
        eventSubscribers.put(DocumentWaldEvent.class.getSimpleName(), new DocumentWaldEventHandler());
    }


    @Override
    public void initialize() throws Exception {
        eventSubscribers.forEach((key, value) -> eventManager.subscribeToEvent(value, key));
    }

    @Override
    public void destroy() throws Exception {
        eventSubscribers.forEach((key, value) -> eventManager.unsubscribeToEvent(value, key));
    }

    private class DocumentWaldEventHandler implements EventSubscriber<DocumentWaldEvent>{
        @Override
        public void onEvent(DocumentWaldEvent event) {
            WALQueueItem walQueueItem = documentQueue.getNext();
            MemTableDataKey dataKey = MemTableDataKey.builder().walID(walQueueItem.getWalID())
                    .logID(walQueueItem.getLogID()).rowKey(walQueueItem.getRowKey()).build();
            try{
                switch (walQueueItem.getOperationType()){
                    case INSERT:
                        dataStore.insert(dataKey, MemTableDataValue.builder().value(walQueueItem.getValue()).build());
                        break;
                    case UPDATE:
                        dataStore.update(dataKey, MemTableDataValue.builder().value(walQueueItem.getValue()).build());
                        break;
                    case DELETE:
                        dataStore.delete(dataKey);
                        break;
                }
                documentQueue.ack(Collections.singletonList(walQueueItem.getId()));
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
    }
}
