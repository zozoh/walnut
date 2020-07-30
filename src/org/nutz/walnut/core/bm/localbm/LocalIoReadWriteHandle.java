package org.nutz.walnut.core.bm.localbm;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import org.nutz.lang.Files;
import org.nutz.lang.Streams;
import org.nutz.walnut.core.bm.WnIoReadWriteHandle;
import org.nutz.walnut.util.Wn;

public class LocalIoReadWriteHandle extends WnIoReadWriteHandle {

    private LocalIoBM bm;

    /**
     * 交换文件
     */
    private File swap;

    private RandomAccessFile raf;

    private FileChannel chan;

    LocalIoReadWriteHandle(LocalIoBM bm) {
        this.bm = bm;
    }

    @Override
    protected FileChannel channel() throws IOException {
        if (null != obj && null == swap) {
            swap = bm.createSwapFile();
            // 不是虚桶的话，要复制一份过来
            if (!Wn.Io.isEmptySha1(obj.sha1())) {
                File buck = bm.checkBucketFile(obj.sha1());
                Files.copy(buck, swap);
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
        bm.updateObjSha1(this.obj, swap, indexer);

        // 重置成员
        swap = null;
        raf = null;
        chan = null;
    }

}
