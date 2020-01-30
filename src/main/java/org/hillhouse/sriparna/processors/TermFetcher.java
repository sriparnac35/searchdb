package org.hillhouse.sriparna.processors;

import java.util.List;

public interface TermFetcher {
    List<String> fetchTermsInString(String document);
}
