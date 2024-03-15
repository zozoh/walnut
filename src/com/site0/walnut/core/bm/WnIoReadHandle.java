package com.site0.walnut.core.bm;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import com.site0.walnut.util.Wlang;
import org.nutz.lang.Streams;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.core.WnIoHandle;
import com.site0.walnut.core.WnIoHandleMutexException;

public abstract class WnIoReadHandle extends WnIoHandle {

    abstract protected InputStream input() throws FileNotFoundException;

    public void ready() throws WnIoHandleMutexException {}

    @Override
    public long skip(long n) throws IOException {
        // 已然关闭
        if (null == obj) {
            throw Er.create("e.io.hdl.closed", this.getId());
        }
        long n2 = input().skip(n);
        this.offset += n2;
        return n2;
    }

    @Override
    public long seek(long n) throws IOException {
        throw Wlang.noImplement();
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
        // 肯定已经关闭过了
        if (null == obj) {
            return;
        }
        obj = null; // 标志一下，这个句柄实例就不能再使用了
        Streams.safeClose(input());
    }

}
