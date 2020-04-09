package org.hillhouse.sriparna.processors;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hillhouse.sriparna.interfaces.DocumentProcessor;
import org.hillhouse.sriparna.interfaces.EventSubscriber;
import org.hillhouse.sriparna.interfaces.IDDao;
import org.hillhouse.sriparna.models.Event;
import org.hillhouse.sriparna.models.WALEntry;
import org.hillhouse.sriparna.models.evnets.DataPersistedEvent;
import org.hillhouse.sriparna.models.input.UserOperation;

import java.io.*;

@RequiredArgsConstructor
@Slf4j
public class WalProcessor implements DocumentProcessor<UserOperation>, EventSubscriber<DataPersistedEvent> {
    private static final String FILE_PREFIX = "wal";
    private String fileName ;
    @Inject private final IDDao walIDDao;
    @Inject private final IDDao logIDDao;

    private File file;
    private FileOutputStream fileOutputStream;
    private ObjectOutputStream objectOutputStream;

    @Override
    public void process(UserOperation data) throws IOException{
        String id = logIDDao.getNextID();
        WALEntry walEntry = createWalEntryWithID(id, data);
        addToWAL(walEntry);
    }

    @Override
    public void onEvent(DataPersistedEvent event) {
        try {
            deleteCurrentWAL();
            createNewWal();
        }catch (Exception e){
            // log
        }
    }

    private void deleteCurrentWAL() throws IOException{
        objectOutputStream.close();
        fileOutputStream.close();
        file.delete();
    }

    private void createNewWal() throws Exception {
        String id = walIDDao.getNextID();
        fileName = FILE_PREFIX + id;
        file = new File(fileName);
        boolean isCreated = file.createNewFile();
        if (!isCreated || !file.setReadable(true) || !file.setWritable(true)){
            throw new RuntimeException("wal not created");
        }
        file = new File(fileName);
        fileOutputStream = new FileOutputStream(file);
        objectOutputStream = new ObjectOutputStream(fileOutputStream);
    }

    @Override
    public void initialize() throws Exception{
        createNewWal();
    }

    @Override
    public void destroy() throws Exception{
        objectOutputStream.close();
        fileOutputStream.close();
    }

    private WALEntry createWalEntryWithID(String id, UserOperation data){
        return new WALEntry(id, data.getOperationType(), data.getDocument().getText());
    }
    private void addToWAL(WALEntry walEntry) throws IOException {
        objectOutputStream.write("\n".getBytes());
        objectOutputStream.writeObject(walEntry);
        objectOutputStream.flush();
    }
}
