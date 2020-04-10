package main.interfaces.eventSystem;

import main.models.events.Event;

public interface EventSubscriber<T extends Event> {
    void onEvent(T event);
}
