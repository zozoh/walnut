package com.site0.walnut.core;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnIoIndexer;
import com.site0.walnut.api.io.WnObj;

/**
 * 句柄类由桶的实现类构建。桶的实现类会在这个类构建或第一次写入时，分配缓冲
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public abstract class WnIoHandle extends HandleInfo implements Closeable, Flushable {

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

    /**
     * IO 接口，用来跨越索引管理器更新同步时间（某些写句柄需要）
     */
    protected WnIo io;

    public WnIo getIo() {
        return io;
    }

    public void setIo(WnIo io) {
        this.io = io;
    }

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
     * 当句柄对象被创建，且相关成员都被设置好后。这个函数会被调用
     * <p>
     * 实现类可以通过句柄管理器持久化自己，或者检查有没有重复<br>
     * 譬如写句柄，这里是一个好时机去判断是否有其他的写句柄存在，如果存在就抛错
     */
    public abstract void ready() throws WnIoHandleMutexException, IOException;

    /**
     * 从当前位置偏移
     * 
     * @param n
     *            偏移的字节数
     * @return 实际的偏移量
     * @throws IOException
     */
    public abstract long skip(long n) throws IOException;

    /**
     * 移动到某个指定的位置
     * <p>
     * !!! 注意，这个函数与 setOffset 不同。它会检查边界，并更新句柄管理器的 touch 导致句柄过期时间更新。<br>
     * 而<code>setOffset</code>仅仅是设置一个内存里的值，并且不检查边界
     * 
     * 
     * @param n
     *            移动到位置（字节下标）
     * @return 实际移动到的位置。相当于（getOffset()）
     * @throws IOException
     */
    public abstract long seek(long n) throws IOException;

    /**
     * 读取到缓冲
     * 
     * @param buf
     *            缓冲
     * @param off
     *            偏移（从缓冲何处开始写入）
     * @param len
     *            最多读取多少字节
     * @return 实际读取的字节数，-1 表示不在有字节可以读取了
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

}
