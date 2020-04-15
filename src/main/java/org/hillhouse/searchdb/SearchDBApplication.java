package org.hillhouse.searchdb;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import lombok.NoArgsConstructor;
import org.hillhouse.searchdb.impl.eventHandlers.MemtableSinkWrapper;
import org.hillhouse.searchdb.impl.eventHandlers.MemtableSourceWrapper;
import org.hillhouse.searchdb.impl.eventHandlers.SSTableEventWrapper;

import java.io.IOException;

@NoArgsConstructor
public class SearchDBApplication {
    @Inject private SearchDB searchDB;
    @Inject private MemtableSourceWrapper memtableSourceWrapper;
    @Inject private MemtableSinkWrapper memtableSinkWrapper;
    @Inject private SSTableEventWrapper ssTableEventWrapper;

    public static void main(String[] args) throws Exception {
        Injector injector = Guice.createInjector(new Module());
        SearchDBApplication application = injector.getInstance(SearchDBApplication.class);
        application.run();
    }
    public void run() throws Exception {
        ssTableEventWrapper.initialize();
        memtableSinkWrapper.initialize();
        memtableSourceWrapper.initialize();
        searchDB.initialize();
        searchDB.insert("key", "value");
        searchDB.insert("key1", "value1");
        searchDB.insert("key2", "value2");

        Thread.sleep(4000);
        System.out.println(searchDB.search("key"));
        System.out.println(searchDB.search("key1"));
        System.out.println(searchDB.search("key2"));
    }
}
