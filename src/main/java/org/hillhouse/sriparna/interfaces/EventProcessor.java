package org.hillhouse.sriparna.interfaces;

import main.interfaces.eventSystem.EventPublisher;
import main.models.events.Event;
import org.hillhouse.sriparna.enums.EventType;


public interface EventProcessor extends Initializable {
    void notifyEvent(EventPublisher eventPublisher, Event event);
    void subscribeToEvent(EventType eventType, EventSubscriber subscriber);
    void unsubscribeFromEvent(EventType eventType, EventSubscriber subscriber);
}
