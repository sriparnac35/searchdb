package main.models.wal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import main.models.input.Document;
import main.models.input.OperationType;


@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class WalEntry {
    private long id;
    private OperationType operationType;
    private Document document;
    private WalStatus status;
    private long localTimestamp;
}
