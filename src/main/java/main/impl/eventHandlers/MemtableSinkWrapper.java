package main.impl.eventHandlers;

import com.google.inject.Inject;
import lombok.AllArgsConstructor;
import main.interfaces.capabilities.Initializable;
import main.interfaces.eventSystem.EventManager;
import main.interfaces.eventSystem.EventPublisher;
import main.interfaces.eventSystem.EventSubscriber;
import main.models.events.MemTableAvailableForSinkEvent;
import main.models.events.PersistToSSTableEndEvent;
import main.models.events.PersistToSSTableFailedEvent;
import main.models.memory.Memtable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static main.constants.MemtableConstants.MEMTABLE_SINK_INTERVAL_IN_SEC;

public class MemtableSinkWrapper implements EventPublisher, Initializable {
    @Inject private CurrentMemtableWrapper memtableWrapper;
    @Inject private EventManager eventManager;

    private Map<String, EventSubscriber> eventSubscribers;
    private ScheduledExecutorService sinkExecutorService;
    private boolean isSinkInProgress = false;

    {
        eventSubscribers = new HashMap<>();
        eventSubscribers.put(PersistToSSTableEndEvent.class.getSimpleName(), new MemtableSinkedSuccessEventHandler());
        eventSubscribers.put(PersistToSSTableFailedEvent.class.getSimpleName(), new MemtableSinkedFailedEventHandler());
    }

    @Override
    public void initialize() throws Exception {
        addEventSubscribers();
        sinkExecutorService = Executors.newSingleThreadScheduledExecutor();
        sinkExecutorService.schedule(new PublishMemtableForSinkRunnable(), MEMTABLE_SINK_INTERVAL_IN_SEC, TimeUnit.SECONDS);
    }

    @Override
    public void destroy() throws Exception {
        removeEventSubscribers();
        sinkExecutorService.shutdown();
    }

    @Override
    public String getPublisherID() {
        return MemtableSinkWrapper.class.getSimpleName();
    }

    private void addEventSubscribers(){
        eventSubscribers.forEach((key, value) -> eventManager.subscribeToEvent(value, key));
    }

    private void removeEventSubscribers(){
        eventSubscribers.forEach((key, value) -> eventManager.unsubscribeToEvent(value, key));
    }


    public class MemtableSinkedSuccessEventHandler implements EventSubscriber<PersistToSSTableEndEvent> {
        @Override
        public void onEvent(PersistToSSTableEndEvent event) {
            sinkExecutorService.submit(new MemtableSinkSuccessRunnable(event));
        }
    }

    public class MemtableSinkedFailedEventHandler implements EventSubscriber<PersistToSSTableFailedEvent>{

        @Override
        public void onEvent(PersistToSSTableFailedEvent event) {
            sinkExecutorService.submit(new MemtableSinkFailedRunnable(event));
        }
    }

    @AllArgsConstructor
    private class MemtableSinkSuccessRunnable implements Runnable{
        private PersistToSSTableEndEvent event;

        @Override
        public void run() {
            memtableWrapper.getOldTables().pop();
            isSinkInProgress = false;
        }
    }

    @AllArgsConstructor
    private class MemtableSinkFailedRunnable implements Runnable{
        private PersistToSSTableFailedEvent event;

        @Override
        public void run() {
            isSinkInProgress = false;
        }
    }


    private class PublishMemtableForSinkRunnable implements Runnable{
        @Override
        public void run() {
            if (!isSinkInProgress){
                Memtable memtable = memtableWrapper.getOldTables().peek();
                if (memtable != null){
                    isSinkInProgress = true;
                    notifyMemtableAvailableForSink(memtable);
                }
            }
        }

        private void notifyMemtableAvailableForSink(Memtable memtable){
            MemTableAvailableForSinkEvent event = MemTableAvailableForSinkEvent.builder().memTable(memtable).build();
            eventManager.publishEvent(MemtableSinkWrapper.this, event);
        }
    }
}
