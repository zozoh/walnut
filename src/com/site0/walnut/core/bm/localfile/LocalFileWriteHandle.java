package com.site0.walnut.core.bm.localfile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import org.nutz.lang.Streams;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.core.bm.WnIoWriteHandle;
import com.site0.walnut.core.indexer.localfile.WnLocalFileObj;

public class LocalFileWriteHandle extends WnIoWriteHandle {

    private OutputStream ops;

    protected OutputStream outout() throws FileNotFoundException {
        if (null != obj && null == ops) {
            if (obj instanceof WnLocalFileObj) {
                File f = ((WnLocalFileObj) obj).getFile();
                this.ops = Streams.chanOps(f, false);
            }
            // 不能支持的文件类型
            else {
                throw Er.create("e.io.localfile.UnsupportObjType", obj.getClass().getName());
            }
        }
        return ops;
    }

    @Override
    public void on_close() throws IOException {
        // 无论如何，刷一下
        Streams.safeFlush(ops);

        // 关闭交换文件
        Streams.safeClose(ops);
        ops = null;
    }

}
