package org.hillhouse.sriparna.interfaces;

import org.hillhouse.sriparna.models.Event;

public interface EventSubscriber<T extends Event> {
    void onEvent(T event);

}
