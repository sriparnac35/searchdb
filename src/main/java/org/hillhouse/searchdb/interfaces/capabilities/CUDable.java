package org.hillhouse.searchdb.interfaces.capabilities;

import java.io.IOException;

public interface CUDable<K, V> {
    void insert(K key, V value) throws IOException;

    void update(K key, V value) throws IOException;

    void delete(K key) throws IOException;
}
