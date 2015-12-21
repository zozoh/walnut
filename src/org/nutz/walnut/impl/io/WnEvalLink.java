package org.nutz.walnut.impl.io;

import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnSecurity;

public class WnEvalLink implements WnSecurity {

    protected WnIo io;

    public WnEvalLink(WnIo io) {
        this.io = io;
    }

    @Override
    public WnObj enter(WnObj nd) {
        return __eval_obj(nd);
    }

    @Override
    public WnObj access(WnObj nd) {
        return nd;
    }

    // @Override
    // public WnObj view(WnObj nd) {
    // return __eval_obj(nd, false);
    // }

    @Override
    public WnObj read(WnObj nd) {
        return __eval_obj(nd);
    }

    @Override
    public WnObj write(WnObj nd) {
        return __eval_obj(nd);
    }

    // @Override
    // public WnObj remove(WnObj nd) {
    // return __eval_obj(nd, false);
    // }

    protected WnObj __eval_obj(WnObj o) {
        // 处理链接文件
        if (null!=o && o.isLink()) {
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

    @Override
    public boolean test(WnObj nd, int mode) {
        return true;
    }

}
