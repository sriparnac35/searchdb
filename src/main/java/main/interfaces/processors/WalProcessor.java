package main.interfaces.processors;

import main.interfaces.capabilities.Initializable;
import main.models.input.Document;
import main.models.input.OperationType;


public interface WalProcessor extends Initializable {
    void writeToWal(Document document, OperationType operationType);
    void recoverWal();
}
