package org.hillhouse.sriparna.documentprocessors;

import org.hillhouse.sriparna.processors.Stemmer;

import java.util.List;

public class BaseStemmer implements Stemmer {
    @Override
    public String stem(String word) {
        return word;
    }

    @Override
    public List<String> bulkStem(List<String> wordList) {
        return wordList;
    }
}
