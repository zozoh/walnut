package org.nutz.walnut.core.bm.localfile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.core.bean.WnLocalFileObj;
import org.nutz.walnut.core.bm.WnLocalReadHandle;

public class LocalFileReadHandle extends WnLocalReadHandle {

    private InputStream ins;

    protected InputStream input() throws FileNotFoundException {
        if (null != obj) {
            if (obj instanceof WnLocalFileObj) {
                File f = ((WnLocalFileObj) obj).getFile();
                try {
                    this.ins = Streams.chanIn(f);
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
        return ins;
    }

}
