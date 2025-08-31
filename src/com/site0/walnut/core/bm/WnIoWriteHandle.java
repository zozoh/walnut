package com.site0.walnut.core.bm;

import java.io.IOException;
import java.io.OutputStream;

import com.site0.walnut.util.Wlang;
import org.nutz.lang.Streams;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.core.WnIoHandle;
import com.site0.walnut.core.WnIoHandleMutexException;
import com.site0.walnut.util.Wn;

public abstract class WnIoWriteHandle extends WnIoHandle {

    /**
     * 子类负责创建流，不用负责关闭，本类会管理并缓存这个流对象
     * 
     * @return 输出流
     * @throws IOException
     */
    abstract protected OutputStream getOutputStream() throws IOException;

    private OutputStream _ops;

    private OutputStream ops() throws IOException {
        if (null == _ops) {
            if (null == obj) {
                throw Er.create("e.io.hdl.read.NilObj");
            }
            _ops = getOutputStream();
        }
        return _ops;
    }

    public void ready() throws WnIoHandleMutexException {
        manager.alloc(this);
    }

    @Override
    public long skip(long n) throws IOException {
        throw Wlang.noImplement();
    }

    @Override
    public long seek(long n) throws IOException {
        throw Wlang.noImplement();
    }

    @Override
    public int read() throws IOException {
        throw Wlang.noImplement();
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
        // 写入
        ops().write(buf, off, len);
        this.offset += len;

        // 更新自身过期时间
        this.touch();
    }

    @Override
    public void flush() throws IOException {
        Streams.safeFlush(ops());
    }

    protected abstract void on_close() throws IOException;

    @Override
    public void close() throws IOException {
        // 肯定已经关闭过了
        if (null == obj || null == _ops) {
            return;
        }

        // 获取原始对象的内容指纹
        String oldSha1 = obj.sha1();

        // 关闭流
        Streams.safeFlush(_ops);
        Streams.safeClose(_ops);

        // 调用子类的清除逻辑
        this.on_close();

        // 删除句柄
        manager.remove(this.getId());

        // 如果内容发生变化，更新同步时间
        if (null != io && !obj.isSameSha1(oldSha1)) {
            Wn.Io.update_ancestor_synctime(io, obj, false, 0);
        }

        // 标志一下，这个句柄实例就不能再使用了
        obj = null;
        _ops = null;
    }

}
