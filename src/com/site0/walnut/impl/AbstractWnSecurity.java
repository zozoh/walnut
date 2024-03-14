package com.site0.walnut.impl;

import java.util.HashMap;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnSecurity;
import com.site0.walnut.util.Wn;

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
            if (null == o) {
                return null;
            }
            // 恢复节点的 path，setParent 会自动设置(pid|d0|d1|ph)的
            o.name(oldName);
            o.setParent(oldParent);
        }
        return o;
    }

}
