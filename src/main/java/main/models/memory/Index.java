package main.models.memory;

import javafx.util.Pair;
import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Index implements Serializable {
    @Getter private String ssTable;
    private List<IndexItem> indexItems;

    public Index(String ssTable){
        this.ssTable = ssTable;
        this.indexItems = new ArrayList<>();
    }

    public void addToIndex(IndexItem indexItem){
        this.indexItems.add(indexItem);
    }

    public Pair<Integer, Integer> dataSearchRange(String key){
        return search(key, 0, indexItems.size() - 1);
    }

    private Pair<Integer, Integer> search(String key, int start, int end){
        if (start >= end){
            return null;
        }
        int mid = (end - start) / 2;
        int startCompareValue = indexItems.get(start).getRowKey().compareTo(key);
        int endCompareValue = indexItems.get(end).getRowKey().compareTo(key);
        int midCompareValue = indexItems.get(mid).getRowKey().compareTo(key);

        if (startCompareValue > 0 || endCompareValue < 0){
            return null;
        }
        if (startCompareValue == 0){
            return new Pair<>(start, start);
        }
        if (endCompareValue == 0){
            return new Pair<>(end, end);
        }
        if (midCompareValue == 0){
            return new Pair<>(mid, mid);
        }
        if (midCompareValue < 0){
            Pair<Integer, Integer> value = search(key, mid, end);
            return (value == null) ? new Pair<>(start, end) : value;
        }
        else{
            Pair<Integer, Integer> value = search(key, start, mid);
            return (value == null) ? new Pair<>(start, end) : value;
        }
    }



    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Data
    public static final class IndexItem{
        private String rowKey;
        private Integer offset;
    }
}
