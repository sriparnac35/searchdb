package org.hillhouse.sriparna.interfaces;

import com.sun.org.apache.xml.internal.security.Init;
import main.interfaces.EventPublisher;
import main.models.events.Event;
import org.hillhouse.sriparna.enums.EventType;


public interface EventProcessor extends Initializable {
    void notifyEvent(EventPublisher eventPublisher, Event event);
    void subscribeToEvent(EventType eventType, EventSubscriber subscriber);
    void unsubscribeFromEvent(EventType eventType, EventSubscriber subscriber);
}
