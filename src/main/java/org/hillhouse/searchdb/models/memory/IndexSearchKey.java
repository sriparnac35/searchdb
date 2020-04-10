package org.hillhouse.searchdb.models.memory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class IndexSearchKey {
    private String key;
    private Integer lastSearchedOffset;
}
