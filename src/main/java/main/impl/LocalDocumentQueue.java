package main.impl;

import com.sun.jmx.remote.internal.ArrayQueue;
import main.interfaces.utilities.DocumentQueue;
import main.models.QueueItem;
import main.models.wal.WALQueueItem;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.stream.IntStream;

public class LocalDocumentQueue<T extends QueueItem> implements DocumentQueue<T> {
    private ArrayList<T> queue;

    @Override
    public void push(T data) {
        queue.add(data);
    }

    @Override
    public void push(List<T> data) {
        queue.addAll(data);
    }

    @Override
    public T getNext() {
        return queue.size() > 0 ? queue.get(0) : null;
    }

    @Override
    public List<T> getNext(int count) {
        int num = Math.min(count, queue.size());
        return queue.subList(0, num);
    }

    @Override
    public void ack(List<Long> ids) {
        ids.forEach(item -> queue.remove(item));
    }

    @Override
    public void initialize() throws Exception {
        queue = new ArrayList<>();
    }

    @Override
    public void destroy() throws Exception {
        queue.clear();
    }
}
