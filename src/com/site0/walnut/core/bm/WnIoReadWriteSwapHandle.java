package com.site0.walnut.core.bm;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import org.nutz.lang.Streams;

import com.site0.walnut.util.Wn;

public abstract class WnIoReadWriteSwapHandle extends WnIoReadWriteHandle {

    /**
     * 交换文件
     */
    protected File swap;

    protected RandomAccessFile raf;

    protected FileChannel chan;

    protected abstract File createSwapFile();

    protected abstract void update_when_close(File swap) throws IOException;

    protected abstract void load_to_swap(File swap);

    @Override
    protected FileChannel channel() throws IOException {
        if (null != obj && null == swap) {
            swap = createSwapFile();
            // 不是虚桶的话，要复制一份过来
            if (!Wn.Io.isEmptySha1(obj.sha1())) {
                load_to_swap(swap);
            }
            // 准备通道
            raf = new RandomAccessFile(swap, "rw");
            chan = raf.getChannel();
        }
        return chan;
    }

    @Override
    public void on_close() throws IOException {
        // 无论如何，刷一下
        if (null != chan) {
            chan.force(false);
        }

        // 关闭交换文件
        Streams.safeClose(chan);
        Streams.safeClose(raf);

        // 根据交换文件更新对象的索引
        update_when_close(swap);

        // 重置成员
        swap = null;
        raf = null;
        chan = null;
    }
}
