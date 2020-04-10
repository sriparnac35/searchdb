package org.hillhouse.searchdb.interfaces.capabilities;

import java.io.IOException;

public interface Searchable<K, V> {
    V search(K key) throws IOException;
}
