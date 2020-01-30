package org.hillhouse.sriparna.documentprocessors;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.hillhouse.sriparna.interfaces.DocumentDao;

import java.io.*;


@NoArgsConstructor
public class FileBasedDocumentDao implements DocumentDao {
    private final String directory = "";

    @Override
    public void saveDocument(String id, String document, String version) {
        File file = new File(directory, id + "_" + version);
        try {
            file.createNewFile();
            file.setWritable(true);
            file.setReadable(true);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.write(document.getBytes());
            objectOutputStream.flush();
            objectOutputStream.close();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteDocument(String id) {

    }

    @Override
    public String getDocument(String id, String version) {
        File file = new File(directory, id + "_" + version);
        try{
            FileInputStream fileInputStream = new FileInputStream(file);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            return objectInputStream.readLine();
            // close
        }catch (IOException e){
            throw new RuntimeException(e);
        }

    }
}
