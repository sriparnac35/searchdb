package org.hillhouse.sriparna.models;

import javafx.collections.transformation.SortedList;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public class Index {
    private Map<String, Set<IndexItem>> indexMap;
    private List<String> deletedFiles;
    private Map<String, Long> latestFileVersions;

    public Index(){
        this.indexMap = new HashMap<>();
        this.deletedFiles = new ArrayList<>();
        this.latestFileVersions = new HashMap<>();
    }

    public void addDocumentToIndex(String id, Map<String, Long> wordMap){
        long timestamp = System.currentTimeMillis();
        latestFileVersions.put(id, timestamp);
        wordMap.forEach((key, value) -> {
            Set<IndexItem> set = indexMap.get(id);
            if (set == null){
                set = new TreeSet<>(Comparator.comparing(item -> item.count));
            }
            set.add(new IndexItem(id, timestamp, value));
        });
    }

    public void deleteDocumentFromIndex(String id){
        deletedFiles.add(id);
    }

    public List<String> getDocumentsMatching(String word){
        Set<IndexItem> result = this.indexMap.get(word);
        if (result == null){
            return new ArrayList<>();
        }
        return result.stream()
                .filter(item -> !deletedFiles.contains(item.documentId))
                .filter(item -> item.version == this.latestFileVersions.get(item.documentId))
                .map(item -> item.documentId)
                .collect(Collectors.toList());
    }


    @AllArgsConstructor
    @Data
     private static class IndexItem{
        @NonNull private String documentId;
        private long version;
        private long count;
    }

}
