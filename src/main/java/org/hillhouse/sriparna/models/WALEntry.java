package org.hillhouse.sriparna.models;

import lombok.*;



@AllArgsConstructor
@Getter
public class WALEntry {
    private final String id;
    private final ChangeType changeType;
    private final String data;
}
