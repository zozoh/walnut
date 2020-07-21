package org.nutz.walnut.core;

import java.io.IOException;

import org.nutz.walnut.api.io.WnObj;

/**
 * 句柄类由桶的实现类构建。桶的实现类会在这个类构建或第一次写入时，分配缓冲
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public abstract class WnIoHandle extends HandleInfo {

    /**
     * 句柄管理器，用来更新句柄过期时间
     */
    protected WnIoHandleManager manager;

    /**
     * 索引管理器，用来更新对象
     */
    protected WnIoIndexer indexer;

    /**
     * 句柄处理的对象
     */
    protected WnObj obj;

    public void setManager(WnIoHandleManager manager) {
        this.manager = manager;
    }

    public WnIoIndexer getIndexer() {
        return indexer;
    }

    public void setIndexer(WnIoIndexer indexer) {
        this.indexer = indexer;
    }

    public WnObj getObj() {
        return obj;
    }

    public void setObj(WnObj obj) {
        this.obj = obj;
        this.setTargetId(obj.id());
        this.setMount(obj.mount());
    }

    /**
     * 更新自身过期时间
     */
    protected void touch() {
        manager.touch(this);
    }

    /**
     * 读取到缓冲
     * 
     * @param buf
     *            缓冲
     * @param off
     *            偏移（从缓冲何处开始写入）
     * @param len
     *            最多读取多少字节
     * @return 实际读取的字节数，0 表示不在有字节可以读取了
     */
    public abstract int read(byte[] buf, int off, int len) throws IOException;

    public int read(byte[] buf) throws IOException {
        return read(buf, 0, buf.length);
    }

    /**
     * 写入到缓冲
     * 
     * @param buf
     *            输入字节数组
     * @param off
     *            偏移（从输入的何处开始写入）
     * @param len
     *            最多，写入多少字节。如果超过 buf 的长度，则自动停止
     */
    public abstract void write(byte[] buf, int off, int len) throws IOException;

    public void write(byte[] buf) throws IOException {
        write(buf, 0, buf.length);
    }

    /**
     * 将缓冲中的内容主动写入到对应的桶内。 当然，关闭前
     */
    public abstract void flush() throws IOException;

    /**
     * 关闭一个句柄
     */
    public abstract void close() throws IOException;

}
