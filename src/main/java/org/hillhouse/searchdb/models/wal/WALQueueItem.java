package org.hillhouse.searchdb.models.wal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hillhouse.searchdb.models.QueueItem;
import org.hillhouse.searchdb.models.input.OperationType;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class WALQueueItem extends QueueItem {
    private String walID;
    private int logID;
    private String rowKey;
    private String value;
    private OperationType operationType;
}
