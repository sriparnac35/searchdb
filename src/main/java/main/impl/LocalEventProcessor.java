package main.impl;

import org.hillhouse.sriparna.enums.EventType;
import org.hillhouse.sriparna.interfaces.EventProcessor;
import org.hillhouse.sriparna.interfaces.EventSubscriber;
import org.hillhouse.sriparna.models.Event;

public class LocalEventProcessor implements EventProcessor {
    @Override
    public void notifyEvent(Event event) {

    }

    @Override
    public void subscribeToEvent(EventType eventType, EventSubscriber subscriber) {

    }

    @Override
    public void unsubscribeFromEvent(EventType eventType, EventSubscriber subscriber) {

    }

    @Override
    public void initialize() throws Exception {

    }

    @Override
    public void destroy() throws Exception {

    }
}
