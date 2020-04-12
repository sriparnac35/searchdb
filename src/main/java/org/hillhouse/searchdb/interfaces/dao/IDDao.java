package org.hillhouse.searchdb.interfaces.dao;

import java.util.concurrent.atomic.AtomicInteger;

public interface IDDao {
    AtomicInteger getNextID();
}
