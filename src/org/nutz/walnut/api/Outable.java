package org.nutz.walnut.api;

import java.io.Closeable;
import java.io.Flushable;
import java.io.InputStream;

import org.nutz.json.JsonFormat;

public interface Outable extends Flushable, Closeable {

    void write(InputStream ins);

    void writeAndClose(InputStream ins);

    void write(byte[] b);

    void write(byte[] b, int off, int len);

    void println();

    void println(Object obj);

    void printlnf(String fmt, Object... args);

    void writeJson(Object o, JsonFormat fmt);

    void writeJson(Object o);

    void printf(String fmt, Object... args);

    void print(CharSequence msg);

}