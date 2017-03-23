package org.nutz.walnut.impl.box;

import java.io.IOException;
import java.io.InputStream;

public class EscapeCloseInputStream extends InputStream {

    private InputStream ins;

    public static EscapeCloseInputStream WRAP(InputStream ins) {
        if (null == ins)
            return null;
        if (ins instanceof EscapeCloseInputStream)
            return (EscapeCloseInputStream) ins;
        return new EscapeCloseInputStream(ins);
    }

    public EscapeCloseInputStream(InputStream ins) {
        this.ins = ins;
    }

    public int read() throws IOException {
        return ins.read();
    }

    public int read(byte[] b) throws IOException {
        if (null == ins)
            return -1;
        return ins.read(b);
    }

    public int read(byte[] b, int off, int len) throws IOException {
        if (null == ins)
            return -1;
        return ins.read(b, off, len);
    }

    public long skip(long n) throws IOException {
        if (null == ins)
            return 0;
        return ins.skip(n);
    }

    public int available() throws IOException {
        if (null == ins)
            return 0;
        return ins.available();
    }

    public void mark(int readlimit) {
        if (null != ins)
            ins.mark(readlimit);
    }

    public void reset() throws IOException {
        if (null != ins)
            ins.reset();
    }

    public boolean markSupported() {
        return ins.markSupported();
    }

    public void close() throws IOException {}
}
