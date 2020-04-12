package org.hillhouse.searchdb.impl;

import com.google.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hillhouse.searchdb.constants.Constants;
import org.hillhouse.searchdb.interfaces.capabilities.CUDable;
import org.hillhouse.searchdb.interfaces.dao.DiskDao;
import org.hillhouse.searchdb.interfaces.dao.IDDao;
import org.hillhouse.searchdb.interfaces.eventSystem.EventManager;
import org.hillhouse.searchdb.interfaces.eventSystem.EventPublisher;
import org.hillhouse.searchdb.interfaces.eventSystem.EventSubscriber;
import org.hillhouse.searchdb.interfaces.processors.WalProcessor;
import org.hillhouse.searchdb.interfaces.utilities.DocumentQueue;
import org.hillhouse.searchdb.interfaces.utilities.Mapper;
import org.hillhouse.searchdb.models.events.*;
import org.hillhouse.searchdb.models.wal.entries.WalDataEntry;
import org.hillhouse.searchdb.models.wal.entries.WalStateEntry;
import org.hillhouse.searchdb.models.wal.enums.WALOperationType;
import org.hillhouse.searchdb.models.wal.entries.WalEntry;
import org.hillhouse.searchdb.models.wal.enums.WalCommitStatus;
import org.hillhouse.searchdb.models.wal.enums.WalEntryType;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class LocalWalProcessor implements WalProcessor, CUDable<String, String>, EventPublisher {

    @Inject private DocumentQueue<WALQueueItem> documentQueue;

    private Map<String, EventSubscriber> subscribers ;
    {
        subscribers = new HashMap<>();
        subscribers.put(PersistToSSTableBeginEvent.class.getSimpleName(), new PersistStartEventHandler());
        subscribers.put(PersistToSSTableEndEvent.class.getSimpleName(), new PersistSuccessfulEventHandler());
        subscribers.put(PersistToSSTableFailedEvent.class.getSimpleName(), new PersistFailedEventHandler());
    }

    @Override
    public void insert(String key, String value) throws IOException {
        WalDataEntry entry = createWALDataEntry(key, value, WALOperationType.INSERT);
        writeToStore(entry);
        handleWalWriteStatus(entry);
    }

    @Override
    public void update(String key, String value) throws IOException {
        WalDataEntry entry = createWALDataEntry(key, value, WALOperationType.UPDATE);
        writeToStore(entry);
        handleWalWriteStatus(entry);
    }

    @Override
    public void delete(String key) throws IOException {
        WalDataEntry entry = createWALDataEntry(key, null, WALOperationType.DELETE);
        writeToStore(entry);
        handleWalWriteStatus(entry);
    }

    @Override
    public void initialize() throws Exception {

    }

    @Override
    public void destroy() throws Exception {

    }

    @Override
    public String getPublisherID() {
        return getClass().getSimpleName();
    }

    private WalDataEntry createWALDataEntry(String key, String value, WALOperationType operationType){
        return WalDataEntry.builder().rowKey(key).value(value).operationType(operationType)
                .entryType(WalEntryType.DATA).logID(localIDDao.getNextID()).build();
    }

    private WalStateEntry createWALStateEntry(WalCommitStatus commitStatus, int beginOffset, int endOffset){
        return WalStateEntry.builder().logID(localIDDao.getNextID()).entryType(WalEntryType.COMMIT_STATE)
                .beginOffset(beginOffset).endOffset(endOffset).commitStatus(commitStatus).build();
    }

    private void writeToStore(WalDataEntry walDataEntry) throws IOException{

    }

    private void writeToStore(WalStateEntry walEntry) throws IOException{

        byte[] byteArray = toByteArrayMapper.map(walEntry);
        try{
            diskDao.writeAndSyncToLatest(byteArray);
            return true;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }


    private class PersistStartEventHandler implements EventSubscriber<PersistToSSTableBeginEvent>{
        @Override
        public void onEvent(PersistToSSTableBeginEvent event) {
            WalStateEntry entry = createWALStateEntry(WalCommitStatus.COMMIT_BEGIN, event.getBeginLogID(), event.getEndLogID());
            writeToStore(entry);
        }
    }

    private class PersistSuccessfulEventHandler implements EventSubscriber<PersistToSSTableEndEvent>{
        @Override
        public void onEvent(PersistToSSTableEndEvent event) {
            WalStateEntry entry = createWALStateEntry(WalCommitStatus.COMMIT_END, event.getBeginLogID(), event.getEndLogID());
            writeToStore(entry);
        }
    }

    private class PersistFailedEventHandler implements EventSubscriber<PersistToSSTableFailedEvent>{
          @Override
         public void onEvent(PersistToSSTableFailedEvent event) {
            WalStateEntry entry = createWALStateEntry(WalCommitStatus.COMMIT_FAILED, event.getBeginLogID(), event.getEndLogID());
            writeToStore(entry);
        }
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
    public void onEvent(PersistToSSTableEndEvent event) {
        WalEntry walEntry = WalEntry.builder().status(WalCommitStatus.COMMIT_END).build();
        writeToStore(walEntry);
    }

    private void handleWalWriteStatus(WalEntry walEntry){
        handleWalWriteSuccess(walEntry);
    }

    private void handleWalWriteSuccess(WalEntry walEntry){
        WALQueueItem walQueueItem = walItemMapper.map(walEntry);
        documentQueue.push(walQueueItem);
        DocumentWaldEvent event = new DocumentWaldEvent(walEntry.getLogID());
        eventManager.publishEvent(this, event);
    }

    private void handleWalWriteFailed(WalEntry walEntry){
        DocumentWaldEvent event = new DocumentWaldEvent(walEntry.getLogID());
        eventManager.publishEvent(this, event);
    }




    private void handleWAlRecovered(List<WalEntry> walEntries) throws Exception {
        WalCommitStatus walStatus = getWalStatus(walEntries);
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

    private WalCommitStatus getWalStatus(List<WalEntry> walEntries){
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
