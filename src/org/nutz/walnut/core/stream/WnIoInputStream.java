package org.nutz.walnut.core.stream;

import java.io.IOException;
import java.io.InputStream;

import org.nutz.lang.Lang;
import org.nutz.walnut.core.WnIoHandle;

public class WnIoInputStream extends InputStream {

    private WnIoHandle h;

    public WnIoInputStream(WnIoHandle h) {
        this.h = h;
    }

    @Override
    public int read() throws IOException {
        throw Lang.noImplement();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return h.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return h.read(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        return h.skip(n);
    }

    @Override
    public void close() throws IOException {
        h.close();
    }

}