package org.hillhouse.searchdb.models.wal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hillhouse.searchdb.models.wal.entries.WalEntry;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class WalSearchValue {
    private List<WalEntry> dataValues;
}
