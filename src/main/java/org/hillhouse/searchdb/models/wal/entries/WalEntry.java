package org.hillhouse.searchdb.models.wal.entries;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hillhouse.searchdb.models.wal.enums.WalEntryType;

import java.io.Serializable;


@NoArgsConstructor
@AllArgsConstructor
@Data
@SuperBuilder
public class WalEntry implements Serializable {
    private String walID;
    private int logID;
    private WalEntryType entryType;
}
