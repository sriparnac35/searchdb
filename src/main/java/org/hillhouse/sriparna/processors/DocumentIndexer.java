package org.hillhouse.sriparna.processors;

import lombok.RequiredArgsConstructor;
import org.hillhouse.sriparna.configs.SystemConfig;
import org.hillhouse.sriparna.daos.SSDocumentDao;
import org.hillhouse.sriparna.interfaces.Initializable;
import org.hillhouse.sriparna.models.Document;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DocumentIndexer implements Initializable {
    private final SystemConfig systemConfig;
    private final Queue<Document[]> pendingDocumentQueues ;
    private final Executor executor;
    private final SSDocumentDao documentDao;
    private Document[] current;
    private int currentDocCount = 0;


    public DocumentIndexer(SystemConfig systemConfig) {
        this.systemConfig = systemConfig;
        this.pendingDocumentQueues = new LinkedList<>();
        this.executor = Executors.newSingleThreadExecutor();
        this.documentDao = new SSDocumentDao();
    }

    public synchronized void addNewDocument(Document document){
        createOrClearCurrentDocumentHolder();
        int indexToAddDocument = findIndexToAddDocument(document);
        addDocumentAtIndex(document, indexToAddDocument);
    }

    private void createOrClearCurrentDocumentHolder(){
        if (current == null){
            current = new Document[systemConfig.getDiskSizeOnMerge()];
        }
        if (currentDocCount == systemConfig.getInmemoryDataSize()){
            pendingDocumentQueues.add(current);
            current = new Document[systemConfig.getDiskSizeOnMerge()];
        }
    }

    private void addDocumentAtIndex(Document document, int index){
        for (int i = currentDocCount; i > 0 && i >= index; i--){
            current[i] = current[i-1];
        }
        current[index] = document;
        currentDocCount++;
    }

    private int findIndexToAddDocument(Document document){
        String documentIndex = document.getId();
        int left = 0, right = currentDocCount, mid = (right - left) / 2;
        while (mid >= left && mid <= right){
            String midID = current[mid].getId();
            int compareValue = documentIndex.compareTo(midID);
            if (compareValue == 0){
                return mid;
            }
            else if (compareValue < 0){
                left = mid;
            }
            else {
                right = mid ;
            }
            mid = (right - left) / 2;
        }
        return mid;
    }

    @Override
    public void initialize() throws Exception {
        documentDao.initialize();
        executor.execute(new TaskRunnable());
    }

    @Override
    public void destroy() throws Exception {

    }

    private class TaskRunnable implements Runnable{

        @Override
        public void run() {
            Document[] document = pendingDocumentQueues.peek();
            try{
                documentDao.publishDocuments(document);
                pendingDocumentQueues.remove();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }





}
