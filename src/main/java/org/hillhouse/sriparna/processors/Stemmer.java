package org.hillhouse.sriparna.processors;

import java.util.List;

public interface Stemmer {
    String stem(String word);
    List<String> bulkStem(List<String> wordList);
}
