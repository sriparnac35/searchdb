package org.hillhouse.searchdb.models.memory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class MemTableDataKey {
    private String rowKey;
    private String walID;
    private int logID;
}
