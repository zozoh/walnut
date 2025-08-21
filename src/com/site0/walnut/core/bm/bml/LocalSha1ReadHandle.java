package com.site0.walnut.core.bm.bml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.Streams;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.core.bm.WnIoReadHandle;
import com.site0.walnut.util.Wn;

public class LocalSha1ReadHandle extends WnIoReadHandle {

    private LocalSha1BM bm;

    LocalSha1ReadHandle(LocalSha1BM bm) {
        this.bm = bm;
    }

    // 因为要考虑到滞后设置 obj，所以在第一次读取的时候，才初始化流
    protected InputStream getInputStream() throws FileNotFoundException {
        WnObj o = this.obj;
        // 虚桶
        if (Wn.Io.isEmptySha1(o.sha1())) {
            return Wlang.ins("");
        }
        // 获取文件
        else {
            File buck = bm.checkBucketFile(o.sha1());
            return Streams.chan(new FileInputStream(buck));
        }
    }

}
