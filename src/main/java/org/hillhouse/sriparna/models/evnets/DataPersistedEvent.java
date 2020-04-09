package org.hillhouse.sriparna.models.evnets;

import lombok.NonNull;
import org.hillhouse.sriparna.models.Event;

public class DataPersistedEvent extends Event {
    private String logID;

    public DataPersistedEvent(@NonNull String eventType, @NonNull String logID, @NonNull Long timestamp) {
        super(eventType, timestamp);
        this.logID = logID;
    }
}
