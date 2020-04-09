package main.interfaces.dao;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public interface IDDao {
    long getNextID();
}
