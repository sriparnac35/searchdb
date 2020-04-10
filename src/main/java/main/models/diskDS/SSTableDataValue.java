package main.models.diskDS;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class SSTableDataValue {
    private List<SSTableDataValueItem> dataValueItems;
}
