package org.hillhouse.searchdb;

import com.google.inject.Guice;
import com.google.inject.Injector;

import java.io.IOException;

public class App {
    public static void main(String[] args) throws IOException {
        Injector injector = Guice.createInjector(new Module());

    }


}
