package org.hillhouse.searchdb.interfaces.processors;

import org.hillhouse.searchdb.interfaces.capabilities.Initializable;
import org.hillhouse.searchdb.models.input.Document;
import org.hillhouse.searchdb.models.input.OperationType;


public interface WalProcessor extends Initializable {
    void writeToWal(Document document, OperationType operationType);
    void recoverWal();
}
