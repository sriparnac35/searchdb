package org.hillhouse.searchdb.models;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@Data
@Builder
public class QueueItem implements Serializable {
    public long id;
}
