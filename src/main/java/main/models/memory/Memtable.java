package main.models.memory;

import lombok.*;
import main.algo.BST;
import main.interfaces.capabilities.Readable;
import main.models.QueueItem;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Memtable extends QueueItem implements Readable<String, Memtable.DataItem> {
    private BST<String, DataItem> data;
    @Getter @Setter private String walID;
    @Getter @Setter private int beginLogID;
    @Getter @Setter private int endLogID;

    public Memtable(long id){
        this.id = id;
        data = new BST<>(Comparator.comparing(String::toString));
    }

    public int size(){
        return data.getCount();
    }

    @Override
    public List<DataItem> readAll() {
        return data.getAll();
    }

    @Override
    public int count() {
        return data.getCount();
    }

    @Override
    public DataItem search(String key) throws IOException {
        return data.search(key);
    }

    public void insert(DataItem dataItem){
        data.insert(dataItem);
    }


    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Builder
    public static final class DataItem implements BST.NodeItem<String>{
        private String rowID;
        private boolean isDeleted;
        private String value;

        @Override
        public String getID() {
            return rowID;
        }
    }
}
