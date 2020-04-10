package org.hillhouse.searchdb.models.diskDS;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class SSTableDataKey {
    private String walID;
    private String tableIdentifier;
    private int startID;
    private int endID;
}
