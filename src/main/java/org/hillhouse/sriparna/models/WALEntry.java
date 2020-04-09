package org.hillhouse.sriparna.models;

import lombok.*;
import main.models.QueueItem;

import java.io.Serializable;


@AllArgsConstructor
@Getter
public class WALEntry implements Serializable {
    private final String id;
    private final OperationType operationType;
    private final String data;
}
