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
            // 准备保持旧的几个关键字段信息
            WnObj oldParent = o.parent();
            String oldName = o.name();
            // 展开链接文件
            o = Wn.real(o, io, new HashMap<String, WnObj>());
            // 恢复节点的 path，setParent 会自动设置(pid|d0|d1|ph)的
            o.name(oldName);
            o.setParent(oldParent);
        }
        return o;
    }

}
