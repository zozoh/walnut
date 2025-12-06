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

    protected OutputStream getOutputStream() throws FileNotFoundException {
        if (obj instanceof WnLocalFileObj) {
            File f = ((WnLocalFileObj) obj).getFile();
            return Streams.chanOps(f, false);
        }
        // 不能支持的文件类型
        else {
            throw Er.create("e.io.localfile.UnsupportObjType",
                            obj.getClass().getName());
        }
    }

    @Override
    public void on_close() throws IOException {
        // 没啥好做的
    }

}
