package main.models.wal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import main.models.QueueItem;
import main.models.input.OperationType;

import java.util.concurrent.atomic.AtomicInteger;

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
