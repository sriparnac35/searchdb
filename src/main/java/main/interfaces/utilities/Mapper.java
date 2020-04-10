package main.interfaces.utilities;

@FunctionalInterface
public interface Mapper<I, O> {
    O map(I input);
}
