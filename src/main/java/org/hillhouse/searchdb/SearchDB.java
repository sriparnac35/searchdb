package org.hillhouse.searchdb;

import com.google.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.hillhouse.searchdb.impl.DocumentRetriever;
import org.hillhouse.searchdb.impl.datastores.WALDataStore;
import org.hillhouse.searchdb.impl.eventHandlers.WalWrapper;
import org.hillhouse.searchdb.interfaces.processors.DataStore;

import java.io.IOException;

@NoArgsConstructor
@AllArgsConstructor
public class SearchDB implements DataStore<String, String, String, String> {
    @Inject private WalWrapper dataStore;
    @Inject private DocumentRetriever documentRetriever;

    @Override
    public void insert(String key, String value) throws IOException {
        dataStore.insert(key, value);
    }

    @Override
    public void update(String key, String value) throws IOException {
        dataStore.update(key, value);
    }

    @Override
    public void delete(String key) throws IOException {
        dataStore.delete(key);
    }

    @Override
    public void initialize() throws Exception {
        dataStore.initialize();
    }

    @Override
    public void destroy() throws Exception {
        dataStore.initialize();
    }

    @Override
    public String search(String key) throws IOException {
        return documentRetriever.search(key);
    }
}
