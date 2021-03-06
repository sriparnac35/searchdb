package org.hillhouse.searchdb.models.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hillhouse.searchdb.models.memory.Index;

@NoArgsConstructor
@AllArgsConstructor
@Data
@SuperBuilder
public class NewIndexAvailableEvent extends Event {
    private String ssTableName;
    private Index index;
}
