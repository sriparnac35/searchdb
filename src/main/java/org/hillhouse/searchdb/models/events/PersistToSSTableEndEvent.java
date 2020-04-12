package org.hillhouse.searchdb.models.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class PersistToSSTableEndEvent extends Event {
    private String ssTableName;
    private String walID;
    private int beginLogID;
    private int endLogID;
}
