package org.hillhouse.searchdb.interfaces.capabilities;

import java.util.List;

public interface Readable<K, V> extends Searchable<K, V> {
    List<V> readAll();

    int count();
}
