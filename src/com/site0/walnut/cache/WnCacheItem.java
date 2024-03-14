package com.site0.walnut.cache;

public interface WnCacheItem<T> {

    boolean isExpired();

    /**
     * @return 缓存项权重，权重越高，就越不会被删除
     */
    int getWeight();

    int increaseWeight();

    T getData();

    boolean hasPrev();

    boolean hasNext();

    WnCacheItem<T> prev();

    WnCacheItem<T> next();

    void setPrev(WnCacheItem<T> prev);

    void setNext(WnCacheItem<T> next);

}
