package org.hillhouse.sriparna.models;

import lombok.*;

import java.io.Serializable;


@AllArgsConstructor
@Getter
public class WALEntry implements Serializable {
    private final String id;
    private final ChangeType changeType;
    private final String data;
}
