package main.interfaces.processors;

import main.interfaces.capabilities.CUDable;
import main.interfaces.capabilities.Initializable;
import main.interfaces.capabilities.Searchable;

public interface DataStore<K,V, S, T> extends Initializable, CUDable<K, V>, Searchable<S, T> {
}
