package org.hillhouse.searchdb.interfaces.eventSystem;

import org.hillhouse.searchdb.models.events.Event;

public interface EventSubscriber<T extends Event> {
    void onEvent(T event);
}
