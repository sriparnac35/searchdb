package org.hillhouse.searchdb.models.diskDS;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class DiskDataValue {
    private String rowKey;
    private String value;
}
