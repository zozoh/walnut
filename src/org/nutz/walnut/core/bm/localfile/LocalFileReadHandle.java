package org.nutz.walnut.core.bm.localfile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import org.nutz.lang.Streams;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.core.bean.WnLocalFileObj;
import org.nutz.walnut.core.bm.WnIoReadHandle;

public class LocalFileReadHandle extends WnIoReadHandle {

    private InputStream ins;

    protected InputStream input() throws FileNotFoundException {
        if (null != obj && null == ins) {
            if (obj instanceof WnLocalFileObj) {
                File f = ((WnLocalFileObj) obj).getFile();
                this.ins = Streams.chanIn(f);
            }
            // 不能支持的文件类型
            else {
                throw Er.create("e.io.localfile.UnsupportObjType", obj.getClass().getName());
            }
        }
        return ins;
    }

}
