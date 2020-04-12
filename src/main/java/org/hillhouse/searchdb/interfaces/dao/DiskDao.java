package org.hillhouse.searchdb.interfaces.dao;

import org.hillhouse.searchdb.interfaces.capabilities.Initializable;

import java.io.IOException;

public interface DiskDao extends Initializable {
    void createWithName(String name) throws IOException;

    void deleteWithName(String name) throws IOException;

    void makeCurrent(String name) throws IOException;

    void writeAndSyncToLatest(byte[] data) throws IOException;

    byte[] getDataFromLatest() throws Exception;

    byte[] getData(String name, int start, int end) throws IOException;
}
