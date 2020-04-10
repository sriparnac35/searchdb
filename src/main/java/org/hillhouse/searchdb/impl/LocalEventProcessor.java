package org.hillhouse.searchdb.impl;

import lombok.AllArgsConstructor;
import org.hillhouse.searchdb.interfaces.capabilities.Initializable;
import org.hillhouse.searchdb.interfaces.eventSystem.EventManager;
import org.hillhouse.searchdb.interfaces.eventSystem.EventPublisher;
import org.hillhouse.searchdb.interfaces.eventSystem.EventSubscriber;
import org.hillhouse.searchdb.models.events.Event;
import org.hillhouse.searchdb.enums.EventType;
import org.hillhouse.searchdb.interfaces.EventProcessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LocalEventProcessor implements EventManager, Initializable {
    private Map<String, Subscriptions> eventSubscribers;

    @Override
    public void publishEvent(EventPublisher eventPublisher, Event event) {
        Subscriptions subscriptions = eventSubscribers.get(event.getName());
        if (subscriptions != null){
            subscriptions.executorService.submit(new EventNotificationRunnable(event, subscriptions.eventSubscribers));
        }
    }

    @Override
    public void subscribeToEvent(EventSubscriber eventSubscriber, String eventName) {
        Subscriptions subscriptions = eventSubscribers.get(eventName);
        if (subscriptions == null){
            synchronized (this){
                if (subscriptions == null){
                    subscriptions = new Subscriptions();
                    eventSubscribers.put(eventName, subscriptions);
                }
            }
        }
        subscriptions.eventSubscribers.add(eventSubscriber);
    }

    @Override
    public void unsubscribeToEvent(EventSubscriber eventSubscriber, String eventName) {
        Subscriptions subscriptions = eventSubscribers.get(eventName);;
        if (subscriptions != null) {
            subscriptions.eventSubscribers.remove(eventSubscriber);
        }
    }

    @Override
    public void initialize() throws Exception {
        eventSubscribers = new HashMap<>();
    }

    @Override
    public void destroy() throws Exception {
        eventSubscribers.values().forEach(item -> item.executorService.shutdown());
        eventSubscribers.clear();
    }

    @AllArgsConstructor
    private static class EventNotificationRunnable implements Runnable{
        private Event event;
        private List<EventSubscriber> subscribers;

        @Override
        public void run() {
            subscribers.forEach(item -> item.onEvent(event));
        }
    }

    private static class Subscriptions{
        List<EventSubscriber> eventSubscribers;
        ExecutorService executorService;

        public Subscriptions(){
            this.eventSubscribers = new ArrayList<>();
            executorService = Executors.newSingleThreadScheduledExecutor();
        }

    }

}
