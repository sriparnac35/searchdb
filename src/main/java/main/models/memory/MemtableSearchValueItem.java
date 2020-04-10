package main.models.memory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class MemtableSearchValueItem {
    private String rowKey;
    private String value;
    private boolean isDeleted;
}
