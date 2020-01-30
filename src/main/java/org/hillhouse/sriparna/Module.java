package org.hillhouse.sriparna;

import com.google.inject.AbstractModule;
import org.hillhouse.sriparna.documentprocessors.BaseStemmer;
import org.hillhouse.sriparna.documentprocessors.FileBasedDocumentDao;
import org.hillhouse.sriparna.documentprocessors.WordBasedStopWordRemover;
import org.hillhouse.sriparna.documentprocessors.WordBasedTermFetcher;
import org.hillhouse.sriparna.interfaces.DocumentDao;
import org.hillhouse.sriparna.processors.Stemmer;
import org.hillhouse.sriparna.processors.StopWordRemover;
import org.hillhouse.sriparna.processors.TermFetcher;

public class Module extends AbstractModule {

    @Override
    protected void configure() {
        bind(Stemmer.class).to(BaseStemmer.class);
        bind(DocumentDao.class).to(FileBasedDocumentDao.class);
        bind(StopWordRemover.class).to(WordBasedStopWordRemover.class);
        bind(TermFetcher.class).to(WordBasedTermFetcher.class);
    }
}
