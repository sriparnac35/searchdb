package org.hillhouse.searchdb.models.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hillhouse.searchdb.models.memory.Memtable;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class MemTableAvailableForSinkEvent extends Event {
    private Memtable memTable;
}
