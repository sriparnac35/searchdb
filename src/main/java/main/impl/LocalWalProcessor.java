package main.impl;

import com.google.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.constants.Constants;
import main.interfaces.*;
import main.interfaces.dao.DiskDao;
import main.interfaces.dao.IDDao;
import main.models.events.*;
import main.models.input.Document;
import main.models.input.OperationType;
import main.models.wal.WALQueueItem;
import main.models.wal.WalEntry;
import main.models.wal.WalStatus;
import org.hillhouse.sriparna.interfaces.EventProcessor;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;


@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class LocalWalProcessor implements WalProcessor, EventPublisher, EventSubscriber<PersistToSSTableEndEvent> {
    @Inject private DiskDao diskDao;
    @Inject private IDDao localIDDao;

    @Inject private IDDao fileIDDao;
    @Inject private EventManager eventManager;
    @Inject private DocumentQueue<WALQueueItem> documentQueue;
    @Inject private Mapper<WalEntry, byte[]> toByteArrayMapper;
    @Inject private Mapper<WalEntry, WALQueueItem> walItemMapper;
    @Inject private Mapper<byte[], List<WalEntry>> fromByteArrayMapper;

    @Override
    public void writeToWal(Document document, OperationType operationType) {
        long id = localIDDao.getNextID();
        WalEntry walEntry = createWalEntry(id, operationType, document);
        boolean isWriteSuccess = writeToStore(walEntry);
        handleWalWriteStatus(walEntry, isWriteSuccess);
    }

    @Override
    public void recoverWal() {
        try{
            byte[] data = diskDao.getDataFromLatest();
            List<WalEntry> walEntries = fromByteArrayMapper.map(data);
            handleWAlRecovered(walEntries);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            notifyWalRecoveryFailed();
        }
    }

    @Override
    public void initialize() throws Exception {
        eventManager.subscribeToEvent(this, PersistToSSTableEndEvent.class.getSimpleName());
        diskDao.initialize();
    }

    @Override
    public void destroy() throws Exception {
        eventManager.unsubscribeToEvent(this, PersistToSSTableEndEvent.class.getSimpleName());
        diskDao.destroy();
    }

    @Override
    public String getPublisherID() {
        return "WalProcessor";
    }

    @Override
    public void onEvent(PersistToSSTableEndEvent event) {
        WalEntry walEntry = WalEntry.builder().status(WalStatus.COMMIT_END).build();
        writeToStore(walEntry);
    }

    private void handleWalWriteStatus(WalEntry walEntry, boolean isSuccess){
        if (isSuccess){
            handleWalWriteSuccess(walEntry);
        }else {
            handleWalWriteFailed(walEntry);
        }
    }

    private void handleWalWriteSuccess(WalEntry walEntry){
        WALQueueItem walQueueItem = walItemMapper.map(walEntry);
        documentQueue.push(walQueueItem);
        DocumentWaldEvent event = new DocumentWaldEvent(walEntry.getId());
        eventManager.publishEvent(this, event);
    }

    private void handleWalWriteFailed(WalEntry walEntry){
        DocumentWaldEvent event = new DocumentWaldEvent(walEntry.getId());
        eventManager.publishEvent(this, event);
    }

    private boolean writeToStore(WalEntry walEntry) {
        byte[] byteArray = toByteArrayMapper.map(walEntry);
        try{
            diskDao.writeAndSyncToLatest(byteArray);
            return true;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

    private WalEntry createWalEntry(long id, OperationType operationType, Document document) {
        return WalEntry.builder().id(id).operationType(operationType).document(document)
                .localTimestamp(System.currentTimeMillis()).build();
    }
    private void handleWAlRecovered(List<WalEntry> walEntries) throws Exception {
        WalStatus walStatus = getWalStatus(walEntries);
        switch (walStatus){
            case COMMIT_END:
                createNewWal();
                break;
            case IN_PROGRESS:
                publishWAlEntries(walEntries);
                break;
            case COMMIT_BEGIN:
                //TODO
        }
    }

    private void createNewWal() throws Exception{
        String fileName = getNewWalName();
        diskDao.createWithName(fileName);
        diskDao.makeCurrent(fileName);
    }

    private void publishWAlEntries(List<WalEntry> walEntries){
        List<WALQueueItem> queueItems =  walEntries.stream().map(item -> walItemMapper.map(item))
                .collect(Collectors.toList());
        documentQueue.push(queueItems);
        DocumentsRecoveredEvent event = new DocumentsRecoveredEvent();
        eventManager.publishEvent(this, event);
    }

    private WalStatus getWalStatus(List<WalEntry> walEntries){
        WalEntry lastEntry = walEntries.get(walEntries.size() - 1);
        return lastEntry.getStatus();
    }

    private String getNewWalName(){
        return Constants.WAL_PREFIX + fileIDDao.getNextID();
    }

    private void notifyWalRecoveryFailed() {
        Event event = new WalRecoveryFailedEvent();
        eventManager.publishEvent(this, event);
    }
}
