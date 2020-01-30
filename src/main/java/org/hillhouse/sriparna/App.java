package org.hillhouse.sriparna;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.hillhouse.sriparna.processors.IndexCreator;

/**
 * TODO :
 * 1. use operation ids
 * 2. add multiple fields instead of single text like es doc
 * 3. add stemmers
 * 4. document ranker
 * 5. handle updates in mem index
 * 6. put occurrence positions
 * 7. thread safety
 * **/
public class App {
    public static void main( String[] args ) {
        Injector injector = Guice.createInjector(new Module());
        IndexCreator indexCreator = injector.getInstance(IndexCreator.class);
        indexCreator.addNewDocument("1", "This is test", null);
    }
}
