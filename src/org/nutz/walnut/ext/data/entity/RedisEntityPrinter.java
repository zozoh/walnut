package org.nutz.walnut.ext.data.entity;

public interface RedisEntityPrinter<T> {

    void print(T it, int index);

}
