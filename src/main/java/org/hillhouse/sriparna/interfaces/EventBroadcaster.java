package org.hillhouse.sriparna.interfaces;

import org.hillhouse.sriparna.models.Event;

public interface EventBroadcaster {
    void notifyEvent(Event event);
}
