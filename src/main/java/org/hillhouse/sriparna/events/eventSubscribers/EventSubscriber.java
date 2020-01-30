package org.hillhouse.sriparna.events.eventSubscribers;

import org.hillhouse.sriparna.models.Event;

public interface EventSubscriber {
    void onEvent(Event event);

}
