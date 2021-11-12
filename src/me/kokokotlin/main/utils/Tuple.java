package me.kokokotlin.main.utils;

import java.util.List;

public class Tuple<U, V> {
    private final U first;
    private final V second;

    public Tuple(U u, V v) {
        first = u;
        second = v;
    }

    public U getFirst() {
        return first;
    }

    public V getSecond() {
        return second;
    }

    @Override
    public String toString() {
        return "Tuple{" +
                "first=" + first +
                ", second=" + second +
                '}';
    }
}
