package com.site0.walnut.core.bm;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import org.nutz.lang.Files;
import org.nutz.lang.Streams;
import org.nutz.log.Log;

import com.site0.walnut.util.Wlog;
import com.site0.walnut.util.Wn;

public abstract class WnIoReadWriteSwapHandle extends WnIoReadWriteHandle {

    private static final Log log = Wlog.getIO();

    /**
     * 交换文件
     */
    protected File swap;

    protected RandomAccessFile raf;

    protected abstract File createSwapFile();

    protected abstract void update_when_close(File swap) throws IOException;

    protected abstract void load_to_swap(File swap);

    @Override
    protected FileChannel getChannel() throws IOException {
        // 准交换文件
        if (null == swap) {
            swap = createSwapFile();
        }
        // 不是虚桶的话，要复制一份过来
        if (!Wn.Io.isEmptySha1(obj.sha1())) {
            load_to_swap(swap);
        }
        // 准备通道
        raf = new RandomAccessFile(swap, "rw");
        return raf.getChannel();
    }

    @Override
    public void on_close() throws IOException {
        // 关闭交换文件
        Streams.safeClose(raf);

        // 根据交换文件更新对象的索引
        try {
            update_when_close(swap);
        }
        finally {
            try {
                if (null != swap && swap.exists()) {
                    Files.deleteFile(swap);
                }
            }
            finally {
                log.warnf("IoRW: Fail to delete SwapFile: %s", swap);
            }
        }

        // 重置成员
        swap = null;
        raf = null;
    }
}
