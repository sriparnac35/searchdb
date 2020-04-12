package org.hillhouse.searchdb.impl.eventHandlers;

import com.google.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.hillhouse.searchdb.impl.datastores.WALDataStore;
import org.hillhouse.searchdb.interfaces.capabilities.Initializable;
import org.hillhouse.searchdb.interfaces.eventSystem.EventManager;
import org.hillhouse.searchdb.interfaces.eventSystem.EventPublisher;
import org.hillhouse.searchdb.interfaces.eventSystem.EventSubscriber;
import org.hillhouse.searchdb.models.events.PersistToSSTableBeginEvent;
import org.hillhouse.searchdb.models.events.PersistToSSTableEndEvent;
import org.hillhouse.searchdb.models.events.PersistToSSTableFailedEvent;
import org.hillhouse.searchdb.models.wal.WalDataKey;
import org.hillhouse.searchdb.models.wal.WalDataValue;
import org.hillhouse.searchdb.models.wal.WalStateValue;
import org.hillhouse.searchdb.models.wal.WalValue;
import org.hillhouse.searchdb.models.wal.entries.WalStateEntry;
import org.hillhouse.searchdb.models.wal.enums.WalCommitStatus;
import org.hillhouse.searchdb.models.wal.enums.WalEntryType;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@NoArgsConstructor
@AllArgsConstructor
public class WalWrapper implements Initializable, EventPublisher {
    @Inject private EventManager eventManager;
    @Inject private WALDataStore dataStore;

    private ExecutorService executorService;

    private Map<String, EventSubscriber> eventSubscribers = new HashMap<>();
    {
        eventSubscribers.put(PersistToSSTableBeginEvent.class.getSimpleName(), new PersistStartEventHandler());
        eventSubscribers.put(PersistToSSTableEndEvent.class.getSimpleName(), new PersistSuccessfulEventHandler());
        eventSubscribers.put(PersistToSSTableFailedEvent.class.getSimpleName(), new PersistFailedEventHandler());
    }

    @Override
    public void initialize() throws Exception {
        dataStore.initialize();
        executorService = Executors.newSingleThreadExecutor();
        eventSubscribers.forEach((key, value) -> eventManager.subscribeToEvent(value, key));
    }

    @Override
    public void destroy() throws Exception {
        dataStore.destroy();
        executorService.shutdown();
        eventSubscribers.forEach((key, value) -> eventManager.unsubscribeToEvent(value, key));
    }

    @Override
    public String getPublisherID() {
        return getClass().getSimpleName();
    }

    @AllArgsConstructor
    private class WAlInsertRunnable implements Runnable{
        private WalDataKey dataKey;
        private WalValue dataValue;

        @SneakyThrows @Override
        public void run() {
            dataStore.insert(dataKey, dataValue);
        }
    }

    private class PersistStartEventHandler implements EventSubscriber<PersistToSSTableBeginEvent>{
        @Override
        public void onEvent(PersistToSSTableBeginEvent event) {
            WalDataKey key = WalDataKey.builder().entryType(WalEntryType.COMMIT_STATE).walID(event.getWalID()).build();
            WalStateValue value = WalStateValue.builder().commitStatus(WalCommitStatus.COMMIT_BEGIN)
                    .beginOffset(event.getBeginLogID()).endOffset(event.getEndLogID()).build();
            executorService.submit(new WAlInsertRunnable(key, value));
        }
    }

    private class PersistSuccessfulEventHandler implements EventSubscriber<PersistToSSTableEndEvent> {
        @Override
        public void onEvent(PersistToSSTableEndEvent event) {
            WalDataKey key = WalDataKey.builder().entryType(WalEntryType.COMMIT_STATE).walID(event.getWalID()).build();
            WalStateValue value = WalStateValue.builder().commitStatus(WalCommitStatus.COMMIT_END)
                    .beginOffset(event.getBeginLogID()).endOffset(event.getEndLogID()).build();
            executorService.submit(new WAlInsertRunnable(key, value));
        }
    }

    private class PersistFailedEventHandler implements EventSubscriber<PersistToSSTableFailedEvent>{
        @Override
        public void onEvent(PersistToSSTableFailedEvent event) {
            WalDataKey key = WalDataKey.builder().entryType(WalEntryType.COMMIT_STATE).walID(event.getWalID()).build();
            WalStateValue value = WalStateValue.builder().commitStatus(WalCommitStatus.COMMIT_FAILED)
                    .beginOffset(event.getBeginLogID()).endOffset(event.getEndLogID()).build();
            executorService.submit(new WAlInsertRunnable(key, value));
        }
    }

}
