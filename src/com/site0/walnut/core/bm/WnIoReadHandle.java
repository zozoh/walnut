package com.site0.walnut.core.bm;

import java.io.IOException;
import java.io.InputStream;

import com.site0.walnut.util.Wlang;
import org.nutz.lang.Streams;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.core.WnIoHandle;
import com.site0.walnut.core.WnIoHandleMutexException;

public abstract class WnIoReadHandle extends WnIoHandle {

    /**
     * 子类负责创建流，不用负责关闭，本类会管理并缓存这个流对象
     * 
     * @return 输入流
     * @throws IOException
     */
    abstract protected InputStream getInputStream() throws IOException;

    private InputStream _ins;

    private InputStream ins() throws IOException {
        if (null == _ins) {
            if (null == obj) {
                throw Er.create("e.io.hdl.read.NilObj");
            }
            _ins = getInputStream();
        }
        return _ins;
    }

    public void ready() throws WnIoHandleMutexException {}

    @Override
    public long skip(long n) throws IOException {
        // 已然关闭
        if (null == obj) {
            throw Er.create("e.io.hdl.closed", this.getId());
        }
        long n2 = ins().skip(n);
        this.offset += n2;
        return n2;
    }

    @Override
    public long seek(long n) throws IOException {
        throw Wlang.noImplement();
    }

    @Override
    public int read() throws IOException {
        return ins().read();
    }

    @Override
    public int read(byte[] buf, int off, int len) throws IOException {
        // 已然关闭
        if (null == obj) {
            throw Er.create("e.io.hdl.closed", this.getId());
        }
        // 读取
        int re = ins().read(buf, off, len);
        if (re > 0) {
            this.offset += re;
        }

        return re;
    }

    @Override
    public void write(byte[] buf, int off, int len) {
        throw Er.create("e.io.hdl.readonly");
    }

    @Override
    public void flush() {}

    @Override
    public void close() throws IOException {
        // 肯定已经关闭过了
        if (null == obj || null == _ins) {
            return;
        }

        // 关闭流
        Streams.safeClose(_ins);

        // 标志一下，这个句柄实例就不能再使用了
        obj = null;
        _ins = null;

    }

}
