package org.hillhouse.sriparna.processors;

import javafx.util.Pair;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.hillhouse.sriparna.interfaces.Callback;
import org.hillhouse.sriparna.interfaces.DocumentDao;
import org.hillhouse.sriparna.interfaces.Initializable;
import org.hillhouse.sriparna.models.ChangeType;
import org.hillhouse.sriparna.models.Index;
import org.hillhouse.sriparna.models.WALEntry;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class IndexCreator implements Initializable{
    private final WalProcessor walProcessor;
    private final TermFetcher termFetcher;
    private final StopWordRemover stopWordRemover;
    private final Stemmer stemmer;
    private final DocumentDao documentDao;
    private Queue<TaskItem> taskQueue ;
    private Thread thread ;
    private Index index;

    public void addNewDocument(String id, String document, Callback callback){
        addToWAL(id, document, ChangeType.INSERT);
        callback.onDocumentAdded(id);
        addToTaskQueue(id, document, ChangeType.INSERT);
    }

    public void updateDocument(String id, String document, Callback callback){
        addToWAL(id, document, ChangeType.UPDATE);
        callback.onDocumentUpdated(id);
        addToTaskQueue(id, document, ChangeType.UPDATE);
    }

    public void deleteDocument(String id, Callback callback){
        addToWAL(id, null, ChangeType.DELETE);
        callback.onDocumentDeleted(id);
        addToTaskQueue(id, null, ChangeType.UPDATE);
    }



    private void addToWAL(String id, String document, ChangeType changeType){
        WALEntry walEntry = new WALEntry(id, changeType, document);
        walProcessor.addToWAL(walEntry);
    }

    private void addToTaskQueue(String id, String document, ChangeType changeType){
        TaskItem taskItem = new TaskItem(id, changeType, document);
        taskQueue.add(taskItem);
    }

    @Override
    public void initialize() {
        this.index = new Index();
        this.taskQueue = new LinkedList<>();
        this.thread = new Thread(new TaskRunnable());
    }

    @Override
    public void destroy() {

    }

    private void handleNextTask(){
        TaskItem taskItem = taskQueue.poll();
        switch (taskItem.changeType){
            case INSERT: case UPDATE:
                handleUpsert(taskItem);
                break;
            case DELETE:
                handleDelete(taskItem);break;
        }
    }
    private void handleUpsert(TaskItem taskItem){
        documentDao.saveDocument(taskItem.docID, taskItem.document);
        List<String> terms = termFetcher.fetchTermsInString(taskItem.document);
        terms = stopWordRemover.removeStopWords(terms);
        terms = stemmer.bulkStem(terms);
        Map<String, Long> wordMap = terms.stream().collect(Collectors.groupingBy(term -> term,
                        Collectors.counting()));
        index.addDocumentToIndex(taskItem.docID, wordMap);
    }

    private void handleDelete(TaskItem taskItem){
        documentDao.deleteDocument(taskItem.docID);
        index.deleteDocumentFromIndex(taskItem.docID);
    }

    @AllArgsConstructor
    private static class TaskItem{
        String docID;
        ChangeType changeType;
        String document;
    }

    private class TaskRunnable implements Runnable{
        @Override
        public void run() {
            while (true){
                if (taskQueue.size() > 0) {
                    handleNextTask();
                }
            }
        }
    }
}
