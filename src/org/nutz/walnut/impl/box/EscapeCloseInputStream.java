package org.nutz.walnut.impl.box;

import java.io.IOException;
import java.io.InputStream;

public class EscapeCloseInputStream extends InputStream {

    private InputStream ins;

    public EscapeCloseInputStream(InputStream ins) {
        this.ins = ins;
    }

    public int read() throws IOException {
        return ins.read();
    }

    public int read(byte[] b) throws IOException {
        return ins.read(b);
    }

    public int read(byte[] b, int off, int len) throws IOException {
        return ins.read(b, off, len);
    }

    public long skip(long n) throws IOException {
        return ins.skip(n);
    }

    public int available() throws IOException {
        return ins.available();
    }

    public void mark(int readlimit) {
        ins.mark(readlimit);
    }

    public void reset() throws IOException {
        ins.reset();
    }

    public boolean markSupported() {
        return ins.markSupported();
    }

    public void close() throws IOException {}
}
