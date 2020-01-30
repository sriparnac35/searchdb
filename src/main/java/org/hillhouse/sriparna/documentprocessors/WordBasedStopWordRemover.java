package org.hillhouse.sriparna.documentprocessors;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.hillhouse.sriparna.processors.StopWordRemover;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@NoArgsConstructor
public class WordBasedStopWordRemover implements StopWordRemover {
    private final List<String> stopWords =  Arrays.asList("and");

    @Override
    public boolean isStopWord(String word) {
        return stopWords.contains(word);
    }

    @Override
    public List<String> removeStopWords(List<String> wordList) {
        return wordList.stream().filter(word -> !stopWords.contains(word))
                .collect(Collectors.toList());
    }
}
