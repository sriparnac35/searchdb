package org.hillhouse.sriparna.interfaces;

import java.io.IOException;

public interface DocumentProcessor<T> extends Initializable{
    void process(T data) throws IOException;
}
