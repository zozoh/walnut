package org.nutz.walnut.core.bm.redis;

import java.io.IOException;

import org.nutz.lang.util.LinkedByteBuffer;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.core.WnIoHandle;
import org.nutz.walnut.core.WnIoHandleMutexException;

public class RedisReadWriteHandle extends WnIoHandle {

    private RedisBM bm;

    private boolean baseOnOldContent;

    private LinkedByteBuffer bytes;

    RedisReadWriteHandle(RedisBM bm) {
        this.bm = bm;
        this.bytes = new LinkedByteBuffer();
    }

    RedisReadWriteHandle(RedisBM bm, boolean baseOnOldContent) {
        this(bm);
        this.baseOnOldContent = baseOnOldContent;
    }

    @Override
    public void flush() throws IOException {}

    // 因为本实现主要考虑的是效率。且似乎没有什么情境需要加锁。所以就不用句柄管理器持久化了
    @Override
    public void ready() throws WnIoHandleMutexException, IOException {
        // 因为依赖于旧内容，先打开一个读句柄，读取一下
        if (baseOnOldContent) {
            byte[] bs = this.bm.getBytes(this.obj.myId());
            bytes.write(bs);
        }
    }

    @Override
    public long skip(long n) throws IOException {
        // 已然关闭
        if (null == obj) {
            throw Er.create("e.io.hdl.closed", this.getId());
        }
        bytes.skipRead((int) n);
        bytes.skipWrite((int) n);
        return bytes.getReadIndex();
    }

    @Override
    public long seek(long n) throws IOException {
        // 已然关闭
        if (null == obj) {
            throw Er.create("e.io.hdl.closed", this.getId());
        }
        bytes.seekRead((int) n);
        bytes.seekWrite((int) n);
        return bytes.getReadIndex();
    }

    @Override
    public int read(byte[] buf, int off, int len) throws IOException {
        // 已然关闭
        if (null == obj) {
            throw Er.create("e.io.hdl.closed", this.getId());
        }
        return bytes.read(buf, off, len);
    }

    @Override
    public void write(byte[] buf, int off, int len) throws IOException {
        // 已然关闭
        if (null == obj) {
            throw Er.create("e.io.hdl.closed", this.getId());
        }
        bytes.write(buf, off, len);
    }

    @Override
    public void close() throws IOException {
        // 肯定已经关闭过了
        if (null == obj) {
            return;
        }

        // 保存数据
        byte[] bs = this.bytes.toArray();
        this.bm.setBytes(this.obj.myId(), bs);

        // 更新索引
        obj.sha1(this.bytes.sha1sum());
        obj.len(this.bytes.getLimit());
        obj.lastModified(System.currentTimeMillis());
        indexer.set(obj, "^(sha1|len|lm)$");

        // 标志一下，这个句柄实例就不能再使用了
        obj = null;

    }
}
