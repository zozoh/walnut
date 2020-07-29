package org.nutz.walnut.core.bm.localbm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import org.nutz.lang.Streams;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.core.bm.WnIoWriteHandle;

public class LocalIoWriteHandle extends WnIoWriteHandle {

    private LocalIoBM bm;

    /**
     * 交换文件
     */
    private File swap;

    private OutputStream ops;

    LocalIoWriteHandle(LocalIoBM bm) {
        this.bm = bm;
    }

    protected OutputStream outout() throws FileNotFoundException {
        if (null != obj && null == swap) {
            swap = bm.createSwapFile();
            ops = Streams.chanOps(swap, false);
        }
        return ops;
    }

    @Override
    public void close() throws IOException {
        // 肯定已经关闭过了
        if (null == obj) {
            return;
        }
        // 无论如何，刷一下
        Streams.safeFlush(ops);

        // 关闭交换文件
        Streams.safeClose(ops);

        // 根据交换文件更新对象的索引
        WnObj o = this.obj;
        try {
            bm.updateObjSha1(o, swap, indexer);
        }
        // 无论如何，删除句柄
        finally {
            manager.remove(this.getId());
            obj = null; // 标志一下，这个句柄实例就不能再使用了
            swap = null;
            ops = null;
        }
    }

}
