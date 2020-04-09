package main.interfaces;

public interface DataReader<K, V> {
    V read(K key);
}
