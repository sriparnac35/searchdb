package org.hillhouse.sriparna.events;

import lombok.NonNull;
import org.hillhouse.sriparna.interfaces.EventBroadcaster;
import org.hillhouse.sriparna.interfaces.EventSubscriber;
import org.hillhouse.sriparna.models.Event;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class EventManager {
    private Map<String, EventBroadcaster> registeredBroadcasters ;
    private Map<String, Set<EventSubscriber>> registeredEventSubscribers;

    public EventManager(){
        this.registeredBroadcasters = new HashMap<>();
        this.registeredEventSubscribers = new HashMap<>();
    }

    public void registerAsEventSourceFor(@NonNull String eventName, EventBroadcaster broadcaster){
        if (registeredBroadcasters.containsKey(eventName)){
            throw new IllegalArgumentException("event source exists");
        }
        registeredBroadcasters.put(eventName, broadcaster);
    }
    public void deregisterAsEventSourceFor(@NonNull String eventName, EventBroadcaster broadcaster){
        if (registeredBroadcasters.get(eventName) != broadcaster){
            throw new IllegalArgumentException("event source exists");
        }
        registeredBroadcasters.remove(eventName, broadcaster);
    }

    public void subscribeToEventType(@NonNull String name, @NonNull EventSubscriber subscriber){
        Set<EventSubscriber> subscribers = registeredEventSubscribers.get(name);
        if (subscribers == null){
            subscribers = new HashSet<>();
        }
        subscribers.add(subscriber);
        registeredEventSubscribers.put(name, subscribers);
    }

    public void unsubscribeForEventType(@NonNull String name, @NonNull EventSubscriber subscriber){
        Set<EventSubscriber> subscribers = registeredEventSubscribers.get(name);
        if (subscribers != null && subscribers.contains(subscriber)){
            subscribers.remove(subscriber);
        }
        registeredEventSubscribers.put(name, subscribers);
    }

    public void onEvent(@NonNull final Event event){
        Set<EventSubscriber> eventSubscribers = registeredEventSubscribers.get(event.getEventType());
        eventSubscribers.forEach(subscriber -> subscriber.onEvent(event));
    }
}
