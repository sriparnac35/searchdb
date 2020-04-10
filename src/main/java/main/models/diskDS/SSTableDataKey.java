package main.models.diskDS;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.concurrent.atomic.AtomicInteger;

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
