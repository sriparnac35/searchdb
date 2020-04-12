package org.hillhouse.searchdb;

import com.google.inject.Inject;
import org.hillhouse.searchdb.impl.DocumentRetriever;
import org.hillhouse.searchdb.impl.eventHandlers.WalWrapper;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;


@RunWith(MockitoJUnitRunner.class)
public class SearchDBTest {
    private SearchDB searchDB;
    @Mock private WalWrapper dataStore;
    @Mock private DocumentRetriever documentRetriever;

    @Before
    public void setup() throws Exception {
        searchDB = new SearchDB(dataStore, documentRetriever);
        searchDB.initialize();
    }

    @After
    public void tearDown() throws Exception {
        searchDB.destroy();
    }

    @Test
    public void testInsertCallsDatastoreInsert() throws IOException {
        String testKey = "testKey";
        String testValue = "testValue";
        searchDB.insert(testKey, testValue);

        ArgumentCaptor<String> keyArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> valueArgumentCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(dataStore, times(1)).insert(keyArgumentCaptor.capture(), valueArgumentCaptor.capture());
        assertEquals(keyArgumentCaptor.getValue(), testKey);
        assertEquals(valueArgumentCaptor.getValue(), testValue);
    }

    @Test
    public void testUpdateCallsDataStoreUpdate() throws IOException{
        String testKey = "testKey";
        String testValue = "testValue";
        searchDB.update(testKey, testValue);

        ArgumentCaptor<String> keyArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> valueArgumentCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(dataStore, times(1)).update(keyArgumentCaptor.capture(), valueArgumentCaptor.capture());
        assertEquals(keyArgumentCaptor.getValue(), testKey);
        assertEquals(valueArgumentCaptor.getValue(), testValue);
    }

    @Test
    public void testDeleteCallsDataStoreDelete() throws IOException{
        String testKey = "testKey";
        searchDB.delete(testKey);

        ArgumentCaptor<String> keyArgumentCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(dataStore, times(1)).delete(keyArgumentCaptor.capture());
        assertEquals(keyArgumentCaptor.getValue(), testKey);
    }

    @Test
    public void testSearchCallsDocumentRetrieverSearchAndReturnsValue() throws IOException{
        String testKey = "testKey";
        String testValue = "testValue";
        doReturn(testValue).when(documentRetriever).search(testKey);

        String result = searchDB.search(testKey);

        ArgumentCaptor<String> keyArgumentCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(documentRetriever, times(1)).search(keyArgumentCaptor.capture());
        assertEquals(keyArgumentCaptor.getValue(), testKey);
        assertEquals(result, testValue);
    }

}
