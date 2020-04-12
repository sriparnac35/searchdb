package org.hillhouse.searchdb.models.wal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class WalDataValue extends WalValue {
    private String rowKey;
    private String value;
}
