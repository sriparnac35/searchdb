package org.hillhouse.searchdb.impl;

import org.hillhouse.searchdb.interfaces.utilities.DocumentQueue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;

public class LocalDocumentQueue<T> implements DocumentQueue<T> {
    private ConcurrentLinkedDeque<T> queue;

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
        return queue.size() > 0 ? queue.peek() : null;
    }

    @Override
    public void ack(List<Integer> ids) {
        ids.forEach(item -> queue.remove((int) item));
    }

    @Override
    public void initialize() throws Exception {
        queue = new ConcurrentLinkedDeque<>();
    }

    @Override
    public void destroy() throws Exception {
        queue.clear();
    }
}
