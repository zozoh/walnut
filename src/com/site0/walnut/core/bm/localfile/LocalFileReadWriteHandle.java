package com.site0.walnut.core.bm.localfile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import org.nutz.lang.Streams;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.core.bm.WnIoReadWriteHandle;
import com.site0.walnut.core.indexer.localfile.WnLocalFileObj;

public class LocalFileReadWriteHandle extends WnIoReadWriteHandle {

    private RandomAccessFile raf;

    @Override
    protected FileChannel getChannel() throws FileNotFoundException {
        if (obj instanceof WnLocalFileObj) {
            File f = ((WnLocalFileObj) obj).getFile();
            raf = new RandomAccessFile(f, "rw");
            return raf.getChannel();
        }
        // 不能支持的文件类型
        else {
            throw Er.create("e.io.localfile.UnsupportObjType",
                            obj.getClass().getName());
        }
    }

    @Override
    public void on_close() throws IOException {

        // 关闭文件
        Streams.safeClose(raf);

        // 重置成员
        raf = null;

    }

    @Override
    public int read() throws IOException {
        return raf.read();
    }

}
