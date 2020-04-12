package org.hillhouse.searchdb.models.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@Data
@SuperBuilder
public class PersistToSSTableEndEvent extends Event {
    private String ssTableName;
    private String walID;
    private int beginLogID;
    private int endLogID;
}
