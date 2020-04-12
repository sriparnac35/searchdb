package org.hillhouse.searchdb.impl;

import com.google.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hillhouse.searchdb.impl.datastores.IndexDataStore;
import org.hillhouse.searchdb.impl.datastores.MemTableDataStore;
import org.hillhouse.searchdb.impl.datastores.SSTableDataStore;
import org.hillhouse.searchdb.interfaces.capabilities.Searchable;
import org.hillhouse.searchdb.models.diskDS.SSTableDataValue;
import org.hillhouse.searchdb.models.diskDS.SSTableDataValueItem;
import org.hillhouse.searchdb.models.diskDS.SSTableSearchKey;
import org.hillhouse.searchdb.models.memory.IndexSearchKey;
import org.hillhouse.searchdb.models.memory.MemTableDataKey;
import org.hillhouse.searchdb.models.memory.MemtableSearchValue;
import org.hillhouse.searchdb.models.memory.MemtableSearchValueItem;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
@AllArgsConstructor
@Setter
public class DocumentRetriever implements Searchable<String, String> {
    @Inject
    private MemTableDataStore memTableDataStore;
    @Inject
    private IndexDataStore indexDataStore;
    @Inject
    private SSTableDataStore ssTableDataStore;

    @Override
    public String search(String key) throws IOException {
        SearchResult searchResult = findInMemstore(key);
        if (searchResult == null) {
            searchResult = findInSSTable(key);
        }
        return searchResult == null ? null : searchResult.value;
    }

    private SearchResult findInSSTable(String key) throws IOException {
        return findInSSTable(key, -1);
    }

    private SearchResult findInSSTable(String key, int offset) throws IOException {
        IndexSearchKey indexSearchKey = IndexSearchKey.builder().key(key).lastSearchedOffset(offset).build();
        SSTableSearchKey ssTableSearchKey = indexDataStore.search(indexSearchKey);
        if (ssTableSearchKey == null) {
            return SearchResult.builder().isFound(false).build();
        }
        SSTableDataValue dataValue = ssTableDataStore.search(ssTableSearchKey);
        SSTableDataValueItem dataValueItem = dataValue.getDataValueItems().stream().collect(Collectors.toMap(SSTableDataValueItem::getRowKey, item -> item)).get(key);
        if (dataValueItem == null) {
            return findInSSTable(key, indexSearchKey.getLastSearchedOffset());
        }
        return SearchResult.builder().isFound(true).isDeleted(dataValueItem.isDeleted()).value(dataValueItem.getValue()).build();
    }


    private SearchResult findInMemstore(String key) throws IOException {
        MemTableDataKey memTableDataKey = MemTableDataKey.builder().rowKey(key).build();
        MemtableSearchValue searchValue = memTableDataStore.search(memTableDataKey);
        if (searchValue == null || searchValue.getItemList() == null || searchValue.getItemList().size() == 0) {
            return SearchResult.builder().isFound(false).build();
        }
        List<MemtableSearchValueItem> results = searchValue.getItemList();
        MemtableSearchValueItem current = results.get(results.size() - 1);
        return SearchResult.builder().isFound(true).isDeleted(current.isDeleted()).value(current.getValue()).build();
    }

    @AllArgsConstructor
    @Builder
    private static class SearchResult {
        boolean isFound;
        boolean isDeleted;
        String value;
    }


}
