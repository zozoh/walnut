package com.site0.walnut.core.bm.bmv;

import java.io.IOException;
import java.io.InputStream;

import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.core.bm.WnIoReadHandle;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wn;

public class XoSha1ReadHandle extends WnIoReadHandle {

    private XoSha1BM bm;

    public XoSha1ReadHandle(XoSha1BM bm) {
        this.bm = bm;
    }

    @Override
    protected InputStream getInputStream() throws IOException {
        WnObj o = this.obj;
        // 虚桶
        if (Wn.Io.isEmptySha1(o.sha1())) {
            return Wlang.ins("");
        }
        // 获取文件
        else {
            String path = bm.parts.toPath(o.sha1());
            return bm.api.read(path);
        }
    }

}
