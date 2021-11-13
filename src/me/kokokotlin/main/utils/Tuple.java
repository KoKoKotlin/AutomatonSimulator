package me.kokokotlin.main.utils;

import java.util.ArrayList;
import java.util.Collection;
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

    public static <T, W> List<Tuple<T, W>> zip(List<T> c1, List<W> c2) {
        if (c1.size() != c2.size()) throw new IllegalArgumentException("The two Collections have to have the same size!");
        
        List<Tuple<T, W>> res = new ArrayList<>();
        for (int i = 0; i < c1.size(); i++) res.add(new Tuple<>(c1.get(i), c2.get(i)));

        return res;
    }

    @Override
    public String toString() {
        return "Tuple{" +
                "first=" + first +
                ", second=" + second +
                '}';
    }
}
