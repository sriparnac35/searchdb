package org.hillhouse.sriparna.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Document {
    private String id;
    private byte[] data;
    private long timestamp;
    private boolean isDeleted;
}
