package org.nutz.walnut.core.bm;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.nutz.lang.Streams;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.core.WnIoHandle;

public abstract class WnLocalReadHandle extends WnIoHandle {

    abstract protected InputStream input() throws FileNotFoundException;

    @Override
    public long skip(long n) throws IOException {
        // 已然关闭
        if (null == obj) {
            throw Er.create("e.io.hdl.closed", this.getId());
        }
        this.offset += input().skip(n);
        return this.offset;
    }

    @Override
    public int read(byte[] buf, int off, int len) throws IOException {
        // 已然关闭
        if (null == obj) {
            throw Er.create("e.io.hdl.closed", this.getId());
        }
        // 读取
        int re = input().read(buf, off, len);
        if (re > 0) {
            this.offset += re;
        }

        return re;
    }

    @Override
    public void write(byte[] buf, int off, int len) {
        throw Er.create("e.io.bm.localfile.hdl.readonly");
    }

    @Override
    public void flush() {}

    @Override
    public void close() throws IOException {
        obj = null; // 标志一下，这个句柄实例就不能再使用了
        Streams.safeClose(input());
    }

}
