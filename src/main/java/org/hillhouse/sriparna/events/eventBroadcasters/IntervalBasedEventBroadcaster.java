package org.hillhouse.sriparna.events.eventBroadcasters;


import org.hillhouse.sriparna.events.EventManager;

public class IntervalBasedEventBroadcaster extends PollBasedEventBroadcaster {
    public IntervalBasedEventBroadcaster(EventManager eventManager, String eventName, int sleepIntervalInSec) {
        super(eventManager, eventName, sleepIntervalInSec);
    }

    @Override
    protected boolean shouldNotify() {
        return true;
    }
}
