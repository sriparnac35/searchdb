package org.hillhouse.searchdb.models.wal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hillhouse.searchdb.models.wal.enums.WalCommitStatus;

@Data
@SuperBuilder
public class WalStateValue extends WalValue {
    private WalCommitStatus commitStatus;
    private int beginOffset;
    private int endOffset;
}
