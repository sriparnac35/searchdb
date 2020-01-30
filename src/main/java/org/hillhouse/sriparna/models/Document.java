package org.hillhouse.sriparna.models;

import lombok.*;

import java.util.Map;

@AllArgsConstructor
@Builder
@NoArgsConstructor
@Getter
public class Document {
    private String id;
    @Setter private boolean isDeleted;
    private long timestamp;
    private Map<String, String> data;
}
