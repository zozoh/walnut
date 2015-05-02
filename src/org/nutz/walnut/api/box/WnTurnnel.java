package org.nutz.walnut.api.box;

import java.io.Closeable;
import java.io.InputStream;
import java.io.OutputStream;

public interface WnTurnnel extends Closeable {

    InputStream asInputStream();

    OutputStream asOutputStream();

    void close();

    byte read();

    int read(byte[] bs);

    int read(byte[] bs, int off, int len);

    void write(byte b);

    void write(byte[] bs, int off, int len);

    /**
     * @see #write(byte[], int, int)
     */
    void write(byte[] bs);

    /**
     * @return 一共读取过多少字节
     */
    long getReadSum();

}