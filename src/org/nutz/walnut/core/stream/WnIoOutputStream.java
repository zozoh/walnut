package org.nutz.walnut.core.stream;

import java.io.IOException;
import java.io.OutputStream;

import org.nutz.lang.Lang;
import org.nutz.walnut.core.WnIoHandle;

public class WnIoOutputStream extends OutputStream {

    private WnIoHandle h;

    public WnIoOutputStream(WnIoHandle h) {
        this.h = h;
    }

    @Override
    public void write(int b) throws IOException {
        throw Lang.noImplement();
    }

    @Override
    public void write(byte[] b) throws IOException {
        h.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        h.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        h.flush();
    }

    @Override
    public void close() throws IOException {
        h.close();
    }

}
