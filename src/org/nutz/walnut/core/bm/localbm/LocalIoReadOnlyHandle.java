package org.nutz.walnut.core.bm.localbm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.core.bm.WnLocalReadHandle;

public class LocalIoReadOnlyHandle extends WnLocalReadHandle {

    private LocalIoBM bm;

    private InputStream ins;

    LocalIoReadOnlyHandle(LocalIoBM bm) {
        this.bm = bm;
    }

    // 因为要考虑到滞后设置 obj，所以在第一次读取的时候，才初始化流
    protected InputStream input() throws FileNotFoundException {
        WnObj o = this.obj;
        if (null != o && null == ins) {
            // 虚桶
            if (!o.hasSha1()) {
                ins = Lang.ins("");
            }
            // 获取文件
            else {
                File buck = bm.checkBucketFile(o.sha1());
                ins = Streams.chan(new FileInputStream(buck));
            }
        }
        return ins;
    }

}
