package main.models.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import main.models.mem.MemTable;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class MemTableFullEvent extends Event {
    private MemTable memTable;
}
