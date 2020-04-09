package main.models.mem;

import lombok.*;
import main.interfaces.CRUD;
import main.interfaces.Initializable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;



public class MemTable implements Initializable, CRUD<String, String> {
    @Getter private TreeMap<String, DataItem> data;
    @Getter @Setter private String walID;
    @Getter @Setter private AtomicInteger beginLogID;
    @Getter @Setter private AtomicInteger endLogID;

    @Override
    public void insert(String key, String value) {
        DataItem dataItem = DataItem.builder().rowID(key).value(value).isDeleted(false).build();
        data.put(key, dataItem);
    }

    @Override
    public void update(String key, String value) {
        DataItem dataItem = DataItem.builder().rowID(key).value(value).isDeleted(false).build();
        data.put(key, dataItem);
    }

    @Override
    public void delete(String key) {
        DataItem dataItem = DataItem.builder().rowID(key).value(null).isDeleted(true).build();
        data.put(key, dataItem);
    }

    @Override
    public String read(String key) {
        return data.get(key).value;
    }

    @Override
    public void initialize() throws Exception {
        data = new TreeMap<>();
    }

    @Override
    public void destroy() throws Exception {

    }

    public int count(){
        return data.size();
    }

    public List<DataItem> getDataItemList(){
        return new ArrayList<>(data.values());
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Builder
    public static final class DataItem{
        private String rowID;
        private boolean isDeleted;
        private String value;
    }

}
