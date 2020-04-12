package org.hillhouse.searchdb.models.wal.entries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hillhouse.searchdb.models.QueueItem;
import org.hillhouse.searchdb.models.wal.enums.WALOperationType;
import org.hillhouse.searchdb.models.wal.enums.WalEntryType;

@NoArgsConstructor
@Data
@SuperBuilder
public class WalDataEntry extends WalEntry {
    private String rowKey;
    private String value;
    private WALOperationType operationType;
}
