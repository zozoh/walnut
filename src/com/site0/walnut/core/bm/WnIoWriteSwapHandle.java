package com.site0.walnut.core.bm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import org.nutz.lang.Files;
import org.nutz.lang.Streams;

public abstract class WnIoWriteSwapHandle extends WnIoWriteHandle {

    /**
     * 交换文件
     */
    protected File swap;

    protected OutputStream ops;

    protected abstract File createSwapFile();

    @Override
    protected OutputStream outout() throws FileNotFoundException {
        if (null != obj && null == swap) {
            swap = createSwapFile();
            ops = Streams.chanOps(swap, false);
        }
        return ops;
    }

    @Override
    public void on_close() throws IOException {
        // 无论如何，刷一下
        Streams.safeFlush(ops);

        // 关闭交换文件
        Streams.safeClose(ops);

        // 根据交换文件更新对象的索引
        try {
            update_when_close(swap);
        }
        // 确保删除交换文件
        finally {
            if (swap.exists()) {
                Files.deleteFile(swap);
            }
        }

        // 重置成员
        swap = null;
        ops = null;
    }

    protected abstract void update_when_close(File swap) throws IOException;

}
