package org.hillhouse.searchdb.impl;

import org.hillhouse.searchdb.interfaces.utilities.DocumentQueue;
import org.hillhouse.searchdb.models.QueueItem;

import java.util.ArrayList;
import java.util.List;

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
    public void ack(List<Integer> ids) {
        ids.forEach(item -> queue.remove((int) item));
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
