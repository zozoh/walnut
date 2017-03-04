package org.nutz.walnut.impl;

import java.util.HashMap;

import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnSecurity;
import org.nutz.walnut.util.Wn;

public abstract class AbstractWnSecurity implements WnSecurity {

    protected WnIo io;

    public AbstractWnSecurity(WnIo io) {
        this.io = io;
    }

    protected WnObj __eval_obj(WnObj o) {
        // 处理链接文件
        if (null != o && o.isLink()) {
            String oldPath = o.path();
            o = Wn.real(o, io, new HashMap<String, WnObj>());
            // 恢复节点的 path
            o.path(oldPath);
        }
        return o;
    }

}
