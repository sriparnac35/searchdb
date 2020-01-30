package org.hillhouse.sriparna.events.eventBroadcasters;

import org.hillhouse.sriparna.events.EventManager;

import java.io.File;

public class DiskCapacityBasedEventBroadcaster extends PollBasedEventBroadcaster {
    private final String diskDirectoryName;
    private final int maxSizeInBytes;
    private File diskFile ;

    public DiskCapacityBasedEventBroadcaster(EventManager eventManager, String eventName,
                                             int sleepIntervalInSec, String diskDirectory, int maxSizeInKB) {
        super(eventManager, eventName, sleepIntervalInSec);
        this.diskDirectoryName = diskDirectory;
        this.maxSizeInBytes = maxSizeInKB * 1024 * 1024;
    }

    @Override
    public void initialize() {
        super.initialize();
        diskFile = new File(diskDirectoryName);
    }

    @Override
    protected boolean shouldNotify() {
        return diskFile.getTotalSpace() >= maxSizeInBytes;
    }
}
