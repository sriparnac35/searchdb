package main.models.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import main.models.memory.Index;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class NewIndexAvailableEvent extends Event {
    private String ssTableName;
    private Index index;
}
