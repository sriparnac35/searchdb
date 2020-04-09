package main.models.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import main.models.mem.Index;
import org.hillhouse.sriparna.events.EventManager;

import java.util.List;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class NewIndexAvailableEvent extends Event {
    private String ssTableName;
    private Index index;
}
