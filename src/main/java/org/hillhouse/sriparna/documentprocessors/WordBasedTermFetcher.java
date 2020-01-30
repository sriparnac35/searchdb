package org.hillhouse.sriparna.documentprocessors;

import org.hillhouse.sriparna.processors.TermFetcher;

import java.util.Arrays;
import java.util.List;

public class WordBasedTermFetcher implements TermFetcher {
    @Override
    public List<String> fetchTermsInString(String document) {
        return Arrays.asList(document.split(" "));
    }
}
