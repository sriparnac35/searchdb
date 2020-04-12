package org.hillhouse.searchdb.impl.datastores;

import com.google.inject.Inject;
import javafx.util.Pair;
import org.hillhouse.searchdb.constants.IndexConstants;
import org.hillhouse.searchdb.interfaces.processors.DataStore;
import org.hillhouse.searchdb.models.diskDS.SSTableDataKey;
import org.hillhouse.searchdb.models.diskDS.SSTableDataValue;
import org.hillhouse.searchdb.models.diskDS.SSTableSearchKey;
import org.hillhouse.searchdb.models.memory.Index;
import org.hillhouse.searchdb.models.memory.IndexSearchKey;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class IndexDataStore implements DataStore<Integer, String, IndexSearchKey, SSTableSearchKey> {
    @Inject private SSTableDataStore dataStore;
    private List<Index> indexList;

    @Override
    public void insert(Integer key, String value) throws IOException {
        Index index = createNewIndexWithIDFor(key, value);
        indexList.add(index);
    }

    @Override
    public void update(Integer key, String value) throws IOException {
        throw new UnsupportedOperationException("update of index not supported");
    }

    @Override
    public void delete(Integer key) throws IOException {
        indexList.remove(new Index(key));
    }

    @Override
    public void initialize() throws Exception {
        indexList = new ArrayList<>();
    }

    @Override
    public void destroy() throws Exception {
        indexList.clear();
    }

    @Override
    public SSTableSearchKey search(IndexSearchKey key) throws IOException {
        int nextOffset = (key.getLastSearchedOffset() == -1) ? indexList.size() - 1 : key.getLastSearchedOffset() - 1;
        if (nextOffset < 0) {
            return null;
        }
        Index indexToSearch = indexList.get(nextOffset);
        Pair<Integer, Integer> offsets = indexToSearch.dataSearchRange(key.getKey());
        return (offsets == null) ? search(IndexSearchKey.builder().key(key.getKey()).lastSearchedOffset(nextOffset).build()) :
                SSTableSearchKey.builder().startOffset(offsets.getKey()).endOffset(offsets.getValue()).ssTableName(indexToSearch.getSsTable()).build();
    }

    private Index createNewIndexWithIDFor(Integer key, String ssTable) throws IOException {
        Index index = new Index(ssTable, key);
        SSTableSearchKey searchKey = SSTableSearchKey.builder().ssTableName(ssTable).startOffset(0).endOffset(-1).build();
        SSTableDataValue dataValue = dataStore.search(searchKey);
        List<Index.IndexItem> indexItems = IntStream.rangeClosed(0, dataValue.getDataValueItems().size()).filter(i -> i % IndexConstants.GAP_ROW_COUNT == 0)
                .mapToObj(i -> dataValue.getDataValueItems().get(i))
                .map(item -> Index.IndexItem.builder().rowKey(item.getRowKey()).offset(item.getOffset()).build())
                .collect(Collectors.toList());
        index.setIndexItems(indexItems);
        return index;
    }


}
