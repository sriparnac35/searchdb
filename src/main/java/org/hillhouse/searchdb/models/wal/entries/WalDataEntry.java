package org.hillhouse.searchdb.models.wal.entries;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hillhouse.searchdb.models.wal.enums.WALOperationType;

@NoArgsConstructor
@Data
@SuperBuilder
public class WalDataEntry extends WalEntry {
    private String rowKey;
    private String value;
    private WALOperationType operationType;
}
