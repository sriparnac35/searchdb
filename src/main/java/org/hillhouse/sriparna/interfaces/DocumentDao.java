package org.hillhouse.sriparna.interfaces;

public interface DocumentDao {
    void saveDocument(String id, String document, String version);
    void deleteDocument(String id);
    String getDocument(String id, String version);
}
