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

    private boolean isEnd;

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
        if (isEnd)
            return -1;
        if (buf_pos >= buf_sz) {
            buf_sz = io.read(hid, buf);
            if (buf_sz < 0) {
                isEnd = true;
                return -1;
            }
            buf_pos = 0;
        }
        if (buf_sz > 0)
            return buf[buf_pos++];
        return -1;
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (isEnd)
            return -1;

        int re = 0;
        // 如果缓冲有字节，先消费
        if (buf_pos < buf_sz) {
            // 应该读多少
            int n = Math.min(buf_sz - buf_pos, len);

            // copy 字节并计数
            System.arraycopy(buf, buf_pos, b, off, n);
            buf_pos += n;
            len -= n;
            off += n;
            re += n;
        }
        // 真的读文件
        if (len > 0) {
            int re2 = io.read(hid, b, off, len);
            if (re2 > 0)
                re += re2;
            else
                isEnd = true;

            if (re == 0)
                re = re2;
        }
        // 返回
        return re;
    }

    @Override
    public void close() throws IOException {
        if (null != hid) {
            io.close(hid);
            hid = null;
        }
    }
    
    @Override
    public long skip(long n) throws IOException {
        if (n < 1)
            return 0;
        io.seek(hid, io.getPos(hid) + n);
        return n;
    }

}
