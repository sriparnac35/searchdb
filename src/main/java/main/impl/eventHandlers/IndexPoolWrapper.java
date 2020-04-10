package main.impl.eventHandlers;

import com.google.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import main.impl.datastores.IndexDataStore;
import main.interfaces.dao.IDDao;
import main.interfaces.eventSystem.EventManager;
import main.interfaces.eventSystem.EventSubscriber;
import main.interfaces.capabilities.Initializable;
import main.models.events.NewIndexAvailableEvent;
import main.models.events.PersistToSSTableEndEvent;
import main.models.memory.Index;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IndexPoolWrapper implements Initializable {
    @Inject private EventManager eventManager;
    @Inject private IndexDataStore dataStore;
    @Inject private IDDao idDao;

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
    private class PersistToSSTableEndEventHandler implements EventSubscriber<PersistToSSTableEndEvent>{
        @Override
        public void onEvent(PersistToSSTableEndEvent event) {
            try {
                dataStore.insert((int)idDao.getNextID(), event.getSsTableName());
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }


}
