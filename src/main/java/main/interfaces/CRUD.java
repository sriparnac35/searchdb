package main.interfaces;

import java.util.List;

public interface CRUD<K, V> {
    void insert(K key, V value);
    void update (K key, V value);
    void delete(K key);
    V read(K key);
}
