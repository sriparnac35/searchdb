package org.hillhouse.searchdb.interfaces.processors;

import org.hillhouse.searchdb.interfaces.capabilities.CUDable;
import org.hillhouse.searchdb.interfaces.capabilities.Initializable;
import org.hillhouse.searchdb.interfaces.capabilities.Searchable;

public interface DataStore<K, V, S, T> extends Initializable, CUDable<K, V>, Searchable<S, T> {
}
