package org.hillhouse.sriparna.configs;

import lombok.Data;

@Data
public class SystemConfig {
    private int diskSizeOnMerge = 90;
    private int intervalInSecForMerge = 1;
}
