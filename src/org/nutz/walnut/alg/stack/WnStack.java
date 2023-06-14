package org.nutz.walnut.alg.stack;

import java.util.List;
import java.util.function.Predicate;

public interface WnStack<T> {

    void push(T item);

    T pop();

    T peek();

    boolean isEmpty();

    int search(T item);

    public List<T> popUtil(Predicate<T> filter, boolean includesive);

    T[] popUtilAsArray(Predicate<T> filter, boolean includesive, Class<T> classOfT);

    List<T> popAll();

    T[] popAllAsArray(Class<T> classOfT);
}
