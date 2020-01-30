package org.hillhouse.sriparna.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor
@Getter
public class Event {
    @NonNull private String eventType;
    @NonNull private Long timestamp;
}
