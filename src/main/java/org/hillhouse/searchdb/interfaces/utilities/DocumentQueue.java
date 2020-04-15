package org.hillhouse.searchdb.interfaces.utilities;

import org.hillhouse.searchdb.interfaces.capabilities.Initializable;

import java.util.List;

public interface DocumentQueue<T> extends Initializable {
    void push(T data);
    T getNext();
    void ack();
}
