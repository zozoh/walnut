package org.nutz.walnut.core.bm.localfile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import org.nutz.lang.Streams;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.core.bean.WnLocalFileObj;
import org.nutz.walnut.core.bm.WnIoReadWriteHandle;

public class LocalFileReadWriteHandle extends WnIoReadWriteHandle {

    private RandomAccessFile raf;

    private FileChannel chan;

    @Override
    protected FileChannel channel() throws FileNotFoundException {
        if (null != obj) {
            if (null == chan) {
                if (obj instanceof WnLocalFileObj) {
                    File f = ((WnLocalFileObj) obj).getFile();
                    raf = new RandomAccessFile(f, "rw");
                    chan = raf.getChannel();
                }
                // 不能支持的文件类型
                else {
                    throw Er.create("e.io.localfile.UnsupportObjType", obj.getClass().getName());
                }
            }
        }
        return chan;
    }

    @Override
    public void close() throws IOException {
        // 肯定已经关闭过了
        if (null == obj) {
            return;
        }
        // 无论如何，刷一下
        if (null != chan) {
            chan.force(false);
        }

        // 关闭文件
        Streams.safeClose(chan);
        Streams.safeClose(raf);

        // 删除句柄
        manager.remove(this.getId());
        obj = null; // 标志一下，这个句柄实例就不能再使用了
        raf = null;
        chan = null;

    }

}
