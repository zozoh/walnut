package com.site0.walnut.cache;

public interface WnCache<T> {

    String toString();

    T get(String key);

    void put(String key, T data);

    T remove(String key);

    void cleanUp();

    void clearAll();

    int size();

}