package main.impl;

import com.google.inject.Inject;
import javafx.util.Pair;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.interfaces.DataReader;
import main.interfaces.Initializable;
import main.models.diskDS.DiskDataKey;
import main.models.diskDS.DiskDataValue;
import main.models.mem.Index;

import java.util.List;

@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class SSDataReader implements Initializable, DataReader<String, String> {
    @Inject private IndexPool indexPool;
    @Inject private SSTableCreator ssTableCreator;

    @Override
    public String read(String key) {
        for (int i = indexPool.getIndices().size() - 1; i >= 0; i --){
            Index currentIndex = indexPool.getIndices().get(i);
            Pair<Integer, Integer> rangeToSearch = currentIndex.dataSearchRange(key);
            if (rangeToSearch == null){
                continue;
            }
            DiskDataKey dataKey = DiskDataKey.builder().sstableName(currentIndex.getSsTable())
                    .startOffset(rangeToSearch.getKey()).endOffset(rangeToSearch.getValue()).build();
            List<DiskDataValue> result = ssTableCreator.read(dataKey);
            for (DiskDataValue item : result){
                if (item.getRowKey().equals(key)){
                    return item.getValue();
                }
            }
        }
        return null;
    }

    @Override
    public void initialize() throws Exception {

    }

    @Override
    public void destroy() throws Exception {

    }
}
