package org.nutz.walnut.core.bm;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.core.WnIoHandle;
import org.nutz.walnut.core.WnIoHandleMutexException;

public abstract class WnIoReadWriteHandle extends WnIoHandle {

    protected abstract FileChannel channel() throws IOException;

    public void ready() throws WnIoHandleMutexException {
        manager.alloc(this);
    }

    @Override
    public long skip(long n) throws IOException {
        // 已然关闭
        if (null == obj) {
            throw Er.create("e.io.hdl.closed", this.getId());
        }
        // 啥也不需要读
        if (n == 0) {
            return 0;
        }
        // 准备
        FileChannel chan = channel();
        // 计算
        long pos = chan.position();
        this.offset = Math.max(0, pos + (int) n);
        this.offset = Math.min(this.offset, chan.size());
        chan.position(this.offset);

        // 更新位置
        this.touch();

        return this.offset - pos;
    }

    @Override
    public long seek(long n) throws IOException {
        // 已然关闭
        if (null == obj) {
            throw Er.create("e.io.hdl.closed", this.getId());
        }
        // 啥也不需要读
        if (n == 0) {
            return 0;
        }
        // 准备
        FileChannel chan = channel();
        // 计算: 从开始移动
        if (n >= 0) {
            this.offset = Math.max(0, (int) n);
            this.offset = Math.min(this.offset, chan.size());
        }
        // 计算: 从末尾
        else {
            this.offset = chan.size() + n + 1;
            this.offset = Math.max(0, this.offset);
            this.offset = Math.min(this.offset, chan.size());
        }
        chan.position(this.offset);

        // 更新位置
        this.touch();

        return this.offset;
    }

    @Override
    public int read(byte[] bs, int off, int len) throws IOException {
        // 已然关闭
        if (null == obj) {
            throw Er.create("e.io.hdl.closed", this.getId());
        }
        // 啥也不需要读
        if (len <= 0) {
            return 0;
        }
        // 准备
        FileChannel chan = channel();
        ByteBuffer bb = ByteBuffer.wrap(bs, off, len);
        // 读取
        int re = chan.read(bb);
        if (re > 0) {
            this.offset += re;
        }

        // 更新自身过期时间
        this.touch();

        return re;
    }

    @Override
    public void write(byte[] bs, int off, int len) throws IOException {
        // 已然关闭
        if (null == obj) {
            throw Er.create("e.io.hdl.closed", this.getId());
        }
        // 啥也没写
        if (len <= 0) {
            return;
        }
        // 准备
        FileChannel chan = channel();
        ByteBuffer bb = ByteBuffer.wrap(bs, off, len);
        // 写入
        int re = chan.write(bb);
        this.offset += re;

        // 更新自身过期时间
        this.touch();
    }

    @Override
    public void flush() throws IOException {
        FileChannel chan = channel();
        chan.force(false);
    }

}
