package com.site0.walnut.core.bm.localbm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.core.bm.WnIoReadHandle;
import com.site0.walnut.util.Wn;

public class LocalIoReadHandle extends WnIoReadHandle {

    private LocalIoBM bm;

    private InputStream ins;

    LocalIoReadHandle(LocalIoBM bm) {
        this.bm = bm;
    }

    // 因为要考虑到滞后设置 obj，所以在第一次读取的时候，才初始化流
    protected InputStream input() throws FileNotFoundException {
        WnObj o = this.obj;
        if (null != o && null == ins) {
            // 虚桶
            if (Wn.Io.isEmptySha1(o.sha1())) {
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
