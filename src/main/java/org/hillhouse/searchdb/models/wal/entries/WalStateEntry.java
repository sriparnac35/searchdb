package org.hillhouse.searchdb.models.wal.entries;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hillhouse.searchdb.models.wal.enums.WalCommitStatus;

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Data
public class WalStateEntry extends WalEntry {
    private WalCommitStatus commitStatus;
    private int beginOffset;
    private int endOffset;
}
