package org.hillhouse.searchdb.interfaces.processors;

import org.hillhouse.searchdb.interfaces.capabilities.CUDable;
import org.hillhouse.searchdb.interfaces.capabilities.Initializable;
import org.hillhouse.searchdb.models.wal.enums.WALOperationType;


public interface WalProcessor extends Initializable, CUDable<String, String> {
    void recoverWal() throws Exception;
}
