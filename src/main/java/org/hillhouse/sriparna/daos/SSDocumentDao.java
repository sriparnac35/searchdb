package org.hillhouse.sriparna.daos;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.TypeLiteral;
import javafx.util.Pair;
import org.hillhouse.sriparna.interfaces.Initializable;
import org.hillhouse.sriparna.models.Document;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;

public class SSDocumentDao implements Initializable {
    private static final String ssTableDirectory = "/";
    private static final String metaTableName = "meta";
    private static final String ssTablePrefix = "ssTable_";
    private static final String ssTableMetaSuffix = "_meta";
    private ObjectMapper objectMapper;
    private Stack<String> fileStack ;

    private FileOutputStream metaFileOutputStream ;

    public synchronized void publishDocuments(Document[] documents) throws Exception{
        long currentTimestamp = System.currentTimeMillis();
        String filename = ssTablePrefix + currentTimestamp;
        String metaFileName = filename + ssTableMetaSuffix;
        addNewFileEntryToMeta(filename);
        File ssFile = createFileWithName(filename);
        File ssMetaFile = createFileWithName(metaFileName);
        FileOutputStream fileOutputStream = new FileOutputStream(ssFile);
        FileOutputStream metaOutputStream = new FileOutputStream(ssMetaFile);
        List<Pair<String, Pair<Integer, Integer>>> index = new ArrayList<>();
        int currentIndex = 0;
        for (Document document : documents) {
            byte[] data = objectMapper.writeValueAsBytes(document);
            fileOutputStream.write(data);
            index.add(new Pair<>(document.getId(), new Pair<>(currentIndex, data.length)));
            currentIndex =+ data.length;
        }
        metaOutputStream.write(objectMapper.writeValueAsBytes(index));
        fileOutputStream.flush();
        fileOutputStream.close();
        metaOutputStream.flush();
        metaOutputStream.close();
    }

    public Document getDocumentById(String id) throws Exception{
        Iterator<String> iterator = fileStack.iterator();
        while (iterator.hasNext()){
            String fileName = iterator.next();
            String metaFileName = fileName + ssTableMetaSuffix;
            File file = new File(metaFileName);
            List<Pair<String, Pair<Integer, Integer>>> index = objectMapper
                    .readValue(file, new TypeReference<List<Pair<String, Pair<Integer, Integer>>>>(){});
            int item = getDocumentIndex(id, index);
            if (item != -1){
                File ssTableFile = new File(fileName);
                FileInputStream fileInputStream = new FileInputStream(ssTableFile);
                Pair<String, Pair<Integer, Integer>> current = index.get(item);
                int size = current.getValue().getValue();
                byte[] data = new byte[size];
                fileInputStream.read(data, current.getValue().getKey(), size);
                return objectMapper.readValue(data, Document.class);
            }
        }
        return null;
    }


    private File createFileWithName(String fileName) throws Exception{
        File file = new File(ssTableDirectory, fileName);
        if (file.createNewFile() && couldSetMetaFilePermissions(file)){
            return file;
        }
        throw new Exception("could not create file");
    }

    private void addNewFileEntryToMeta(String filename) throws Exception{
        this.fileStack.push(filename);
        this.metaFileOutputStream.write("\r\n".getBytes());
        this.metaFileOutputStream.write(filename.getBytes());
        this.metaFileOutputStream.flush();
        this.metaFileOutputStream.close();
    }

    private File getMetaFile() throws Exception{
        File metaFile = new File(ssTableDirectory, metaTableName);
        if (!metaFile.exists()){
            if (metaFile.createNewFile() || couldSetMetaFilePermissions(metaFile)){
                throw new Exception("count not create meta file");
            }
        }
        return metaFile;
    }

    private boolean couldSetMetaFilePermissions(File metaFile){
        return metaFile.setReadable(true) || metaFile.setWritable(true) || metaFile.setExecutable(false);
    }

    @Override
    public void initialize() throws Exception{
        objectMapper = new ObjectMapper();
        fileStack = new Stack<>();
        File metaFile = getMetaFile();
        metaFileOutputStream = new FileOutputStream(metaFile);
    }

    @Override
    public void destroy() throws Exception{
        metaFileOutputStream.close();
    }

    private int getDocumentIndex(String id, List<Pair<String, Pair<Integer, Integer>>> index) throws Exception{
        int left = 0, right = index.size(), mid = (right - left) / 2;
        while (mid >= left && mid <= right){
            String midID = index.get(mid).getKey();
            int compareValue = id.compareTo(midID);
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
        return -1;
    }
}
