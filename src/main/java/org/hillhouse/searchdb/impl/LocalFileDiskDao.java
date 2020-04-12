package org.hillhouse.searchdb.impl;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.hillhouse.searchdb.interfaces.dao.DiskDao;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

@NoArgsConstructor
public class LocalFileDiskDao implements DiskDao {
    private File currentFile = null;
    private FileInputStream currentFileInputStream = null;
    private FileOutputStream currentFileOutputStream = null;

    @Override
    public void createWithName(String name) throws IOException {
        File file = new File(name);
        if (file.exists()) {
            throw new IOException("count not create file : " + name);
        }
        if (!(file.createNewFile() && file.setReadable(true) && file.setWritable(true) && file.setExecutable(false))) {
            throw new IOException("count not set permissions for file : " + name);
        }
    }

    @Override
    public void deleteWithName(String name) throws IOException {
        File file = new File(name);
        if (!file.exists() || !file.delete()) {
            throw new IOException("could not delete file : " +  name);
        }
    }

    @Override
    public void makeCurrent(String name) throws IOException {
        closeCurrentIfExists();
        this.currentFile = new File(name);
        this.currentFileInputStream = new FileInputStream(currentFile);
        this.currentFileOutputStream = new FileOutputStream(currentFile);
    }

    @Override
    public void writeAndSyncToLatest(byte[] data) throws IOException {
        currentFileOutputStream.write(data);
        currentFileOutputStream.flush();
    }

    @Override
    public byte[] getDataFromLatest() throws Exception {
        currentFileInputStream.reset();
        return Files.readAllBytes(currentFile.toPath());
    }

    @Override
    public byte[] getData(String name, int start, int end) throws IOException {
        int lengthToRead = (end != -1) ? end - start : (int) currentFileInputStream.getChannel().size();
        byte[] data = new byte[lengthToRead];
        currentFileInputStream.read(data, start, lengthToRead);
        return data;
    }

    @Override
    public void initialize() throws Exception {

    }

    @Override
    public void destroy() throws Exception {
        closeCurrentIfExists();
    }

    private void closeCurrentIfExists() throws IOException {
        if (currentFileInputStream != null) {
            currentFileInputStream.close();
        }
        if (currentFileOutputStream != null) {
            currentFileOutputStream.close();
        }
    }
}
