package main.models.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import main.impl.datastores.MemTableDataStore;
import main.models.memory.Memtable;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class MemTableAvailableForSinkEvent extends Event {
    private Memtable memTable;
}
