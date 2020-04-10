package main.interfaces.capabilities;

import java.io.IOException;
import java.util.List;

public interface Searchable<K, V> {
    V search(K key) throws IOException;
}
