package org.nutz.walnut.core.bm;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.core.WnIoHandle;
import org.nutz.walnut.core.WnIoHandleMutexException;
import org.nutz.walnut.util.Wn;

public abstract class WnIoWriteHandle extends WnIoHandle {

    abstract protected OutputStream outout() throws FileNotFoundException;

    public void ready() throws WnIoHandleMutexException {
        manager.alloc(this);
    }

    @Override
    public long skip(long n) throws IOException {
        throw Lang.noImplement();
    }

    @Override
    public long seek(long n) throws IOException {
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

    protected abstract void on_close() throws IOException;

    @Override
    public void close() throws IOException {
        // 肯定已经关闭过了
        if (null == obj) {
            return;
        }

        // 获取原始对象的内容指纹
        String oldSha1 = obj.sha1();

        // 调用子类的清除逻辑
        this.on_close();

        // 删除句柄
        manager.remove(this.getId());

        // 如果内容发生变化，更新同步时间
        if (!obj.isSameSha1(oldSha1)) {
            Wn.Io.update_ancestor_synctime(indexer, obj, false, 0);
        }

        // 标志一下，这个句柄实例就不能再使用了
        obj = null;
    }

}
