package org.hillhouse.sriparna.events.eventSubscribers;

import org.hillhouse.sriparna.interfaces.EventSubscriber;
import org.hillhouse.sriparna.models.Event;

import java.util.LinkedList;
import java.util.Queue;

public class BaseEventSubscriber implements EventSubscriber {
    private Queue<Event> eventQueue = new LinkedList<>();

    @Override
    public void onEvent(Event event) {
        eventQueue.add(event);
    }

    protected Event getNext(){
        return eventQueue.poll();
    }

    protected boolean areEventsAvailable(){
        return eventQueue.size() > 0;
    }

}
