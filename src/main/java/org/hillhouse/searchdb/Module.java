package org.hillhouse.searchdb;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import org.hillhouse.searchdb.impl.LocalDocumentQueue;
import org.hillhouse.searchdb.impl.LocalEventProcessor;
import org.hillhouse.searchdb.impl.LocalFileDiskDao;
import org.hillhouse.searchdb.impl.LocalIDDao;
import org.hillhouse.searchdb.interfaces.dao.DiskDao;
import org.hillhouse.searchdb.interfaces.dao.IDDao;
import org.hillhouse.searchdb.interfaces.eventSystem.EventManager;
import org.hillhouse.searchdb.interfaces.processors.DataStore;
import org.hillhouse.searchdb.interfaces.utilities.DocumentQueue;
import org.hillhouse.searchdb.models.wal.entries.WalDataEntry;
import org.hillhouse.searchdb.models.wrappers.CurrentMemtableWrapper;


public class Module extends AbstractModule {
    @Override
    protected void configure() {
        bind(new TypeLiteral<DataStore<String, String, String, String>>(){}).to(SearchDB.class);
        bind(DiskDao.class).to(LocalFileDiskDao.class);
        bind(IDDao.class).to(LocalIDDao.class);
        bind(CurrentMemtableWrapper.class).toInstance(new CurrentMemtableWrapper());
    }

    @Provides @Singleton
    public EventManager provideEventManager() throws Exception {
        LocalEventProcessor eventProcessor = new LocalEventProcessor();
        eventProcessor.initialize();
        return eventProcessor;
    }

    @Provides @Singleton
    public DocumentQueue<WalDataEntry> provideWalEntryDataQueue() throws Exception {
        DocumentQueue<WalDataEntry> queue = new LocalDocumentQueue<>();
        queue.initialize();
        return queue;
    }
}
