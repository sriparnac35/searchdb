package org.hillhouse.searchdb.models.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class PersistToSSTableFailedEvent extends Event {
    private String walID;
    private int beginLogID;
    private int endLogID;
}
