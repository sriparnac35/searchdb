package org.hillhouse.searchdb.impl;

import org.hillhouse.searchdb.interfaces.dao.IDDao;

import java.util.concurrent.atomic.AtomicInteger;

public class LocalIDDao implements IDDao {
    private final AtomicInteger currentID = new AtomicInteger(1);

    @Override
    public int getNextID() {
        return currentID.getAndDecrement();
    }
}
