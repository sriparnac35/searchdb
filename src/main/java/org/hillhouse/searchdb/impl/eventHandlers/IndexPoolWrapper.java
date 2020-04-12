package org.hillhouse.searchdb.impl.eventHandlers;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.hillhouse.searchdb.impl.datastores.IndexDataStore;
import org.hillhouse.searchdb.interfaces.capabilities.Initializable;
import org.hillhouse.searchdb.interfaces.dao.IDDao;
import org.hillhouse.searchdb.interfaces.eventSystem.EventManager;
import org.hillhouse.searchdb.interfaces.eventSystem.EventSubscriber;
import org.hillhouse.searchdb.models.events.PersistToSSTableEndEvent;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class IndexPoolWrapper implements Initializable {
    @Inject
    private EventManager eventManager;
    @Inject
    private IndexDataStore dataStore;
    @Inject
    private IDDao idDao;

    private Map<String, EventSubscriber> eventSubscribers = new HashMap<>();

    {
        eventSubscribers = new HashMap<>();
        eventSubscribers.put(PersistToSSTableEndEvent.class.getSimpleName(), new PersistToSSTableEndEventHandler());
    }

    @Override
    public void initialize() throws Exception {
        eventSubscribers.forEach((key, value) -> eventManager.subscribeToEvent(value, key));
    }

    @Override
    public void destroy() throws Exception {
        eventSubscribers.forEach((key, value) -> eventManager.unsubscribeToEvent(value, key));
    }

    @Slf4j
    private class PersistToSSTableEndEventHandler implements EventSubscriber<PersistToSSTableEndEvent> {
        @Override
        public void onEvent(PersistToSSTableEndEvent event) {
            try {
                dataStore.insert((int) idDao.getNextID(), event.getSsTableName());
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }


}
