package main.interfaces;

import main.models.input.Document;
import main.models.input.OperationType;


public interface WalProcessor extends Initializable{
    void writeToWal(Document document, OperationType operationType);
    void recoverWal();
}
