package org.hillhouse.searchdb.models.diskDS;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class SSTableDataValueItem {
    private String rowKey;
    private String value;
    private boolean isDeleted;
    private int offset;
}
