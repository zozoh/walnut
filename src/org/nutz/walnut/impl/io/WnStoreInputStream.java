package org.nutz.walnut.impl.io;

import java.io.IOException;
import java.io.InputStream;

import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.util.Wn;

public class WnStoreInputStream extends InputStream {

    private WnIo io;

    private String hid;

    private byte[] buf;

    private int buf_pos;

    private int buf_sz;

    public WnStoreInputStream(WnObj o, WnIo io, long off) {
        this.io = io;
        this.hid = io.open(o, Wn.S.R);
        if (off > 0) {
            io.seek(hid, off);
        }
        this.buf = new byte[8192];
        this.buf_pos = 0;
        this.buf_sz = 0;
    }

    @Override
    public int read() throws IOException {
        if (buf_pos >= buf_sz) {
            buf_sz = io.read(hid, buf);
            buf_pos = 0;
        }
        return buf[buf_pos++];
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int re = 0;
        // 如果缓冲有字节，先消费
        if (buf_pos < buf_sz) {
            // 应该读多少
            int n = Math.min(buf_sz - buf_pos, len);

            // copy 字节并计数
            System.arraycopy(buf, buf_pos, b, off, n);
            len -= n;
            off += n;
            re += n;
        }
        // 真的读文件
        if (len > 0) {
            re += io.read(hid, b, off, len);
        }
        // 返回
        return re;
    }

    @Override
    public void close() throws IOException {
        io.close(hid);
    }

}
