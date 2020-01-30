package org.hillhouse.sriparna.processors;

import java.util.List;

public interface StopWordRemover {
    boolean isStopWord(String word);
    List<String> removeStopWords(List<String> wordList);
}
