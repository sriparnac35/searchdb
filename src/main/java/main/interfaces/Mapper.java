package main.interfaces;

@FunctionalInterface
public interface Mapper<I, O> {
    O map(I input);
}
