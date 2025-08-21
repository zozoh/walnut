package com.site0.walnut.core.bm.localfile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import org.nutz.lang.Streams;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.core.bm.WnIoReadHandle;
import com.site0.walnut.core.indexer.localfile.WnLocalFileObj;

public class LocalFileReadHandle extends WnIoReadHandle {

    protected InputStream getInputStream() throws FileNotFoundException {
        if (obj instanceof WnLocalFileObj) {
            File f = ((WnLocalFileObj) obj).getFile();
            return Streams.chanIn(f);
        }
        // 不能支持的文件类型
        else {
            throw Er.create("e.io.localfile.UnsupportObjType",
                            obj.getClass().getName());
        }
    }

}
