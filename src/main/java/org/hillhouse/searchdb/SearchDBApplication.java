package org.hillhouse.searchdb;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import lombok.NoArgsConstructor;

import java.io.IOException;

@NoArgsConstructor
public class SearchDBApplication {
    @Inject private SearchDB searchDB;

    public static void main(String[] args) throws Exception {
        Injector injector = Guice.createInjector(new Module());
        SearchDBApplication application = injector.getInstance(SearchDBApplication.class);
        application.run();
    }
    public void run() throws Exception {
        searchDB.initialize();
        searchDB.insert("key", "value");
        searchDB.insert("key1", "value1");
    }
}
