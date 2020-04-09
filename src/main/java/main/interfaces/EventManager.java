package main.interfaces;

import main.models.events.Event;

public interface EventManager {
    void publishEvent(EventPublisher eventPublisher, Event event);
    void subscribeToEvent(EventSubscriber eventSubscriber, String eventName);
    void unsubscribeToEvent(EventSubscriber eventSubscriber, String eventName);
}
