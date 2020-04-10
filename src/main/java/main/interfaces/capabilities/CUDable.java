package main.interfaces.capabilities;

import java.io.IOException;
import java.util.List;

public interface CUDable<K, V> {
    void insert(K key, V value) throws IOException;
    void update (K key, V value) throws IOException;
    void delete(K key) throws IOException;
}
