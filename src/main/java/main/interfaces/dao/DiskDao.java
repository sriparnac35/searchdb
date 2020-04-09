package main.interfaces.dao;

import main.interfaces.Initializable;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface DiskDao extends Initializable {
    void createWithName(String name) throws IOException;
    void deleteWithName(String name) throws IOException;
    void makeCurrent(String name) ;

    void writeAndSyncToLatest(byte[] data) throws IOException;
    byte[] getDataFromLatest() throws Exception;

    byte[] getData(String name, int start, int end) throws IOException;
}
