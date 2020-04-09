package main.interfaces;

import main.models.QueueItem;

import java.io.Serializable;
import java.util.List;

public interface DocumentQueue<T extends QueueItem> extends Initializable{
    void push(T data);
    void push(List<T> data);
    T getNext();
    List<T> getNext(int count);
    void ack(List<Long> ids);
}
