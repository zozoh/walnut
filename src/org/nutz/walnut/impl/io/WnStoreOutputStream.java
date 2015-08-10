package org.nutz.walnut.impl.io;

import java.io.IOException;
import java.io.OutputStream;

import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.util.Wn;

public class WnStoreOutputStream extends OutputStream {

    private WnIo io;

    private String hid;

    private byte[] buf;

    private int buf_sz;

    public WnStoreOutputStream(WnObj o, WnIo io, long off) {

        this.io = io;
        this.hid = io.open(o, Wn.S.W);
        if (off != 0) {
            io.seek(hid, off > 0 ? off : o.len());
        }
        this.buf = new byte[8192];
        this.buf_sz = 0;
    }

    @Override
    public void write(int b) throws IOException {
        if (buf_sz >= buf.length) {
            io.write(hid, buf, 0, buf_sz);
            buf_sz = 0;
        }
        buf[buf_sz++] = (byte) b;
    }

    @Override
    public void write(byte[] b) {
        write(b, 0, b.length);
    }

    @Override
    public void write(byte[] b, int off, int len) {
        __apply_buf();
        io.write(hid, b, off, len);
    }

    private void __apply_buf() {
        if (buf_sz > 0) {
            io.write(hid, buf, 0, buf_sz);
            buf_sz = 0;
        }
    }

    @Override
    public void flush() throws IOException {
        __apply_buf();
        io.flush(hid);
    }

    @Override
    public void close() throws IOException {
        __apply_buf();
        io.close(hid);
    }

}
