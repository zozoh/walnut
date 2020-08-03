package org.nutz.walnut.core.bm.redis;

import java.io.IOException;

import org.nutz.lang.Streams;
import org.nutz.lang.util.LinkedByteArray;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.core.WnIoHandle;
import org.nutz.walnut.core.WnIoHandleMutexException;
import org.nutz.walnut.util.Wn;

public class RedisReadWriteHandle extends WnIoHandle {

    private RedisIoBM bm;

    private boolean baseOnOldContent;

    private LinkedByteArray bytes;

    RedisReadWriteHandle(RedisIoBM bm) {
        this.bm = bm;
        this.bytes = new LinkedByteArray();
    }

    RedisReadWriteHandle(RedisIoBM bm, boolean baseOnOldContent) {
        this(bm);
        this.baseOnOldContent = baseOnOldContent;
    }

    @Override
    public void flush() throws IOException {}

    // 因为本实现主要考虑的是效率。且似乎没有什么情境需要加锁。所以就不用句柄管理器持久化了
    @Override
    public void ready() throws WnIoHandleMutexException {
        // 因为依赖于旧内容，先打开一个读句柄，读取一下
        if (baseOnOldContent) {
            WnIoHandle h = null;
            try {
                h = bm.open(obj, Wn.S.R, indexer);
                byte[] buf = new byte[8192];
                int len = 0;
                while ((len = h.read(buf)) >= 0) {
                    bytes.write(buf, 0, len);
                }
            }
            catch (WnIoHandleMutexException e) {
                throw Er.wrap(e);
            }
            catch (IOException e) {
                throw Er.wrap(e);
            }
            finally {
                Streams.safeClose(h);
            }
        }
    }

    @Override
    public long skip(long n) throws IOException {
        return 0;
    }

    @Override
    public long seek(long n) throws IOException {
        return 0;
    }

    @Override
    public int read(byte[] buf, int off, int len) throws IOException {
        return bytes.read(buf, off, len);
    }

    @Override
    public void write(byte[] buf, int off, int len) throws IOException {
        bytes.write(buf, off, len);
    }

    @Override
    public void close() throws IOException {}
}
