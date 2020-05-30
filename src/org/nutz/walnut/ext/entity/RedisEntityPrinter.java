package org.nutz.walnut.ext.entity;

public interface RedisEntityPrinter<T> {

    void print(T it, int index);

}
