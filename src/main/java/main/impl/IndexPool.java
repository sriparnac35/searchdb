package main.impl;

import com.google.inject.Inject;
import lombok.Getter;
import main.interfaces.EventManager;
import main.interfaces.EventSubscriber;
import main.interfaces.Initializable;
import main.models.events.NewIndexAvailableEvent;
import main.models.mem.Index;

import java.util.List;

public class IndexPool implements Initializable, EventSubscriber<NewIndexAvailableEvent> {
    @Inject private EventManager eventManager;
    @Getter private List<Index> indices;

    @Override
    public void onEvent(NewIndexAvailableEvent event) {
        indices.add(event.getIndex());
    }

    @Override
    public void initialize() throws Exception {
        eventManager.subscribeToEvent(this, NewIndexAvailableEvent.class.getSimpleName());
    }

    @Override
    public void destroy() throws Exception {
        eventManager.unsubscribeToEvent(this, NewIndexAvailableEvent.class.getSimpleName());
    }
}
