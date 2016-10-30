package org.nutz.walnut.impl;

import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnSecurity;

public abstract class AbstractWnSecurity implements WnSecurity {

    protected WnIo io;

    public AbstractWnSecurity(WnIo io) {
        this.io = io;
    }

    protected WnObj __eval_obj(WnObj o) {
        // 处理链接文件
        if (null != o && o.isLink()) {
            String oldPath = o.path();
            String ln = o.link();
            // 用 ID
            if (ln.startsWith("id:")) {
                String id = ln.substring("id:".length());
                o = io.get(id);
            }
            // 用路径
            else {
                if (ln.startsWith("/")) {
                    o = io.fetch(null, ln);
                } else {
                    WnObj p = o.parent();
                    o = io.fetch(p, ln);
                }
            }
            // 如果节点不存在
            if (null == o)
                throw Er.create("e.io.obj.noexists", ln);
            // 恢复节点的 path
            o.path(oldPath);
        }
        return o;
    }

}
