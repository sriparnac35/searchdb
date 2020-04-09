package main.models.mem;

import lombok.*;
import main.impl.BST;
import main.interfaces.CRUD;
import main.interfaces.Initializable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;



public class MemTable implements Initializable, CRUD<String, String> {
    @Getter private BST<String, DataItem> data;
    @Getter @Setter private String walID;
    @Getter @Setter private AtomicInteger beginLogID;
    @Getter @Setter private AtomicInteger endLogID;

    @Override
    public void insert(String key, String value) {
        DataItem dataItem = DataItem.builder().rowID(key).value(value).isDeleted(false).build();
        data.insert(dataItem);
    }

    @Override
    public void update(String key, String value) {
        DataItem dataItem = DataItem.builder().rowID(key).value(value).isDeleted(false).build();
        data.insert(dataItem);
    }

    @Override
    public void delete(String key) {
        DataItem dataItem = DataItem.builder().rowID(key).value(null).isDeleted(true).build();
        data.insert(dataItem);
    }

    @Override
    public String read(String key) {
        DataItem dataItem = data.search(key);
        return (dataItem == null) ? null : dataItem.value;
    }

    @Override
    public void initialize() throws Exception {
        data = new BST<>(Comparator.comparing(String::toString));
    }

    @Override
    public void destroy() throws Exception {

    }

    public int count(){
        return data.getCount();
    }

    public List<DataItem> getDataItemList(){
        return data.getAll();
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
