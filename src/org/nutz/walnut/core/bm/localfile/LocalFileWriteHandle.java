package org.nutz.walnut.core.bm.localfile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.core.bean.WnLocalFileObj;
import org.nutz.walnut.core.bm.WnLocalWriteHandle;

public class LocalFileWriteHandle extends WnLocalWriteHandle {

    private OutputStream ops;

    protected OutputStream outout() throws FileNotFoundException {
        if (null != obj) {
            if (obj instanceof WnLocalFileObj) {
                File f = ((WnLocalFileObj) obj).getFile();
                try {
                    this.ops = Streams.chanOps(f, false);
                }
                catch (FileNotFoundException e) {
                    throw Lang.wrapThrow(e);
                }
            }
            // 不能支持的文件类型
            else {
                throw Er.create("e.io.localfile.UnsupportObjType", obj.getClass().getName());
            }
        }
        return ops;
    }

    @Override
    public void close() throws IOException {
        // 无论如何，刷一下
        Streams.safeFlush(ops);

        // 关闭交换文件
        Streams.safeClose(ops);

        // 删除句柄
        manager.remove(this.getId());
        obj = null; // 标志一下，这个句柄实例就不能再使用了
    }

}
