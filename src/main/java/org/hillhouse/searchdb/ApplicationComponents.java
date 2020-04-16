package org.hillhouse.searchdb;

import com.google.inject.Inject;
import org.hillhouse.searchdb.impl.eventHandlers.MemtableSinkWrapper;
import org.hillhouse.searchdb.impl.eventHandlers.MemtableSourceWrapper;
import org.hillhouse.searchdb.impl.eventHandlers.SSTableEventWrapper;
import org.hillhouse.searchdb.interfaces.capabilities.Initializable;

public class ApplicationComponents implements Initializable {
    @Inject private MemtableSourceWrapper memtableSourceWrapper;
    @Inject private MemtableSinkWrapper memtableSinkWrapper;
    @Inject private SSTableEventWrapper ssTableEventWrapper;

    @Override
    public void initialize() throws Exception {
        ssTableEventWrapper.initialize();
        memtableSinkWrapper.initialize();
        memtableSourceWrapper.initialize();
    }

    @Override
    public void destroy() throws Exception {
        ssTableEventWrapper.destroy();
        memtableSinkWrapper.destroy();
        memtableSourceWrapper.destroy();
    }
}
