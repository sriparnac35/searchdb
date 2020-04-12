package org.hillhouse.searchdb.models.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hillhouse.searchdb.models.memory.Memtable;

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Data
public class MemTableAvailableForSinkEvent extends Event {
    private Memtable memTable;
}
