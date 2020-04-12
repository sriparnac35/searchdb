package org.hillhouse.searchdb.models.wal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hillhouse.searchdb.models.wal.enums.WalEntryType;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class WalDataKey {
    private String id;
    private int logID;
    private WalEntryType entryType;
}
