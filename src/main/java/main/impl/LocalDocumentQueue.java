package main.impl;

import main.interfaces.DocumentQueue;
import main.models.wal.WALQueueItem;
import org.hillhouse.sriparna.models.WALEntry;

import java.util.List;

public class LocalDocumentQueue implements DocumentQueue<WALQueueItem> {

    @Override
    public void push(WALQueueItem data) {

    }

    @Override
    public void push(List<WALQueueItem> data) {

    }

    @Override
    public WALQueueItem getNext() {
        return null;
    }

    @Override
    public List<WALQueueItem> getNext(int count) {
        return null;
    }

    @Override
    public void ack(List<Long> ids) {

    }

    @Override
    public void initialize() throws Exception {

    }

    @Override
    public void destroy() throws Exception {

    }
}
