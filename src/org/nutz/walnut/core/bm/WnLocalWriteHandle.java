package org.nutz.walnut.core.bm;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.core.WnIoHandle;

public abstract class WnLocalWriteHandle extends WnIoHandle {

    abstract protected OutputStream outout() throws FileNotFoundException;

    @Override
    public long skip(long n) throws IOException {
        throw Lang.noImplement();
    }

    @Override
    public int read(byte[] buf, int off, int len) throws IOException {
        throw Er.create("e.io.bm.localbm.hdl.writeonly");
    }

    @Override
    public void write(byte[] buf, int off, int len) throws IOException {
        // 已然关闭
        if (null == obj) {
            throw Er.create("e.io.hdl.closed", this.getId());
        }
        // 啥也没写
        if (len <= 0) {
            return;
        }
        // 写入
        this.outout().write(buf, off, len);
        this.offset += len;

        // 更新自身过期时间
        this.touch();
    }

    @Override
    public void flush() throws IOException {
        Streams.safeFlush(outout());
    }

}
