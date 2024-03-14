package com.site0.walnut.core.stream;

import java.io.IOException;
import java.io.OutputStream;

import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.core.WnIoActionCallback;
import com.site0.walnut.core.WnIoHandle;

public class WnIoOutputStream extends OutputStream {

    private WnObj obj;

    private WnIoActionCallback callback;

    private WnIoHandle h;

    private byte[] buf;

    private int bufLen;

    public WnIoOutputStream(WnIoHandle h, WnIoActionCallback callback) {
        this.h = h;
        this.callback = callback;
        this.obj = h.getObj();
        this.buf = new byte[8192];
        this.bufLen = 0;
        if (null != callback) {
            WnObj o = callback.on_before(obj);
            this.obj.updateBy(o);
        }
    }

    @Override
    public void write(int b) throws IOException {
        // 满了
        if (this.bufLen >= buf.length) {
            h.write(buf);
            bufLen = 0;
        }
        // 临时缓存
        this.buf[this.bufLen++] = (byte) b;
    }

    @Override
    public void write(byte[] b) throws IOException {
        this.write(b, 0, b.length);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        // 确保先把缓冲的搞进去
        if (this.bufLen > 0) {
            h.write(buf, 0, this.bufLen);
            bufLen = 0;
        }
        // 写入给定内容
        h.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        // 确保先把缓冲的搞进去
        if (this.bufLen > 0) {
            h.write(buf, 0, this.bufLen);
            bufLen = 0;
        }
        // 刷新柄
        h.flush();
    }

    @Override
    public void close() throws IOException {
        // 确保先把缓冲的搞进去
        if (this.bufLen > 0) {
            h.write(buf, 0, this.bufLen);
            bufLen = 0;
        }

        // 关闭柄
        h.close();

        if (null != callback) {
            WnObj o = callback.on_after(obj);
            this.obj.updateBy(o);
        }
    }

}
