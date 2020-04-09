package org.hillhouse.sriparna.events.eventBroadcasters;

import lombok.SneakyThrows;
import org.hillhouse.sriparna.events.EventManager;
import org.hillhouse.sriparna.interfaces.EventBroadcaster;
import org.hillhouse.sriparna.interfaces.Initializable;
import org.hillhouse.sriparna.models.Event;

public abstract class PollBasedEventBroadcaster implements Initializable, EventBroadcaster {
    private final EventManager eventManager;
    private final String eventName;
    private final int sleepIntervalInSec ;
    private IntervalThread intervalThread;

    public PollBasedEventBroadcaster(EventManager eventManager, String eventName, int sleepIntervalInSec){
        this.eventManager = eventManager;
        this.eventName = eventName;
        this.sleepIntervalInSec = sleepIntervalInSec;
    }

    @Override
    public void initialize() {
        registerAsEventBroadcaster();
        startIntervalLoop();
    }

    @Override
    public void destroy() {
        unregisterAsEventBroadcaster();
        shutdownIntervalLoop();
    }

    private void registerAsEventBroadcaster() {
        eventManager.registerAsEventSourceFor(eventName, this);
    }

    private void unregisterAsEventBroadcaster(){
        eventManager.deregisterAsEventSourceFor(eventName, this);
    }

    private void startIntervalLoop() {
        if (intervalThread == null){
            intervalThread = new IntervalThread(sleepIntervalInSec);
            intervalThread.start();
        }
    }

    private void shutdownIntervalLoop(){
        if (intervalThread != null){
            intervalThread.stopNext();
        }
    }

    private void notifyTimeoutExpired(){
        Event event = new Event(eventName, System.currentTimeMillis());
        eventManager.onEvent(event);
    }

    protected abstract boolean shouldNotify();

    private class IntervalThread extends Thread{
        private final int intervalInSec ;
        private boolean shouldStop = false;
        public IntervalThread(int intervalInSec){
            super();
            this.intervalInSec = intervalInSec;
        }

        @SneakyThrows
        @Override
        public void run(){
            while(!shouldStop){
                this.sleep(intervalInSec * 1000);
                if (shouldNotify()){
                    notifyTimeoutExpired();
                }
            }
        }
        void stopNext(){
            this.shouldStop = true;
        }
    }

}
