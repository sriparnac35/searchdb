package org.hillhouse.searchdb.interfaces.utilities;

import org.hillhouse.searchdb.interfaces.capabilities.Initializable;

import java.util.List;

public interface DocumentQueue<T> extends Initializable {
    void push(T data);

    void push(List<T> data);

    T getNext();

    List<T> getNext(int count);

    void ack(List<Integer> ids);
}
