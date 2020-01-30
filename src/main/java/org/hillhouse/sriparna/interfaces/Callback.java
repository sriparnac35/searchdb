package org.hillhouse.sriparna.interfaces;

public interface Callback {
    void onDocumentAdded(String docID);
    void onDocumentUpdated(String docID);
    void onDocumentDeleted(String docID);
}
