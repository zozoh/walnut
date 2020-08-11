package org.nutz.walnut.core.bm.localfile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import org.nutz.lang.Streams;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.core.bm.WnIoReadWriteHandle;
import org.nutz.walnut.core.indexer.localfile.WnLocalFileObj;

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
    public void on_close() throws IOException {
        // 无论如何，刷一下
        if (null != chan) {
            chan.force(false);
        }

        // 关闭文件
        Streams.safeClose(chan);
        Streams.safeClose(raf);

        // 重置成员
        raf = null;
        chan = null;

    }

}
