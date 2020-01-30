package org.hillhouse.sriparna.processors;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.hillhouse.sriparna.interfaces.Initializable;
import org.hillhouse.sriparna.models.WALEntry;

import java.io.*;

@NoArgsConstructor
public class WalProcessor implements Initializable {
    private final String fileName = "wal";
    private File file;
    private FileOutputStream fileOutputStream;
    private ObjectOutputStream objectOutputStream;

    public void addToWAL(WALEntry walEntry) throws IOException {
        objectOutputStream.write("\n".getBytes());
        objectOutputStream.writeObject(walEntry);
        objectOutputStream.flush();
    }

    @Override
    public void initialize() {
        file = new File(fileName);
        if (!file.exists()){
            createWAL();
        }
        try{
            fileOutputStream = new FileOutputStream(file);
            objectOutputStream = new ObjectOutputStream(fileOutputStream);
        }catch (IOException e){
            throw new RuntimeException();
        }

    }

    @Override
    public void destroy() {
        try{
            objectOutputStream.close();
            fileOutputStream.close();
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    private void createWAL() {
        try {
            boolean isCreated = file.createNewFile();
            if (!isCreated || !file.setReadable(true) || !file.setWritable(true)){
                throw new RuntimeException("wal not created");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
