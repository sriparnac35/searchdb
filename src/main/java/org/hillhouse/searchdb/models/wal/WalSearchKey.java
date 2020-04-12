package org.hillhouse.searchdb.models.wal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class WalSearchKey {
    private String walID;
    private int startOffset;
    private int endOffset;
}
