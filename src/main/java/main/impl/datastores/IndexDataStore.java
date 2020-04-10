package main.impl.datastores;

import com.google.inject.Inject;
import javafx.util.Pair;
import main.algo.BST;
import main.constants.IndexConstants;
import main.interfaces.processors.DataStore;
import main.models.diskDS.SSTable;
import main.models.diskDS.SSTableDataKey;
import main.models.diskDS.SSTableDataValue;
import main.models.diskDS.SSTableDataValueItem;
import main.models.memory.Index;
import main.models.memory.IndexSearchKey;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class IndexDataStore implements DataStore<Integer, String, IndexSearchKey, SSTableDataKey> {
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
    public SSTableDataKey search(IndexSearchKey key) throws IOException {
        int nextOffset = (key.getLastSearchedOffset() == -1) ? indexList.size() - 1 : key.getLastSearchedOffset() - 1;
        if (nextOffset < 0){
            return null;
        }
        Index indexToSearch = indexList.get(nextOffset);
        Pair<Integer, Integer> offsets = indexToSearch.dataSearchRange(key.getKey());
        return (offsets == null) ? search(IndexSearchKey.builder().key(key.getKey()).lastSearchedOffset(nextOffset).build()):
                SSTableDataKey.builder().startID(offsets.getKey()).endID(offsets.getValue()).tableIdentifier(indexToSearch.getSsTable()).build();
    }

    private Index createNewIndexWithIDFor(Integer key, String ssTable) throws IOException {
        Index index = new Index(ssTable, key);
        SSTableDataKey dataKey = SSTableDataKey.builder().tableIdentifier(ssTable).startID(0).endID(-1).build();
        SSTableDataValue dataValue = dataStore.search(dataKey);
        List<Index.IndexItem> indexItems = IntStream.rangeClosed(0, dataValue.getDataValueItems().size()).filter(i -> i % IndexConstants.GAP_ROW_COUNT == 0)
                .mapToObj(i -> dataValue.getDataValueItems().get(i))
                .map(item -> Index.IndexItem.builder().rowKey(item.getRowKey()).offset(item.getOffset()).build())
                .collect(Collectors.toList());
        index.setIndexItems(indexItems);
        return index;
    }


}
