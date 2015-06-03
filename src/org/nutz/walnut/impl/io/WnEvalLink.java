package org.nutz.walnut.impl.io;

import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnNode;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnSecurity;

public class WnEvalLink implements WnSecurity {

    protected WnIo io;

    public WnEvalLink(WnIo io) {
        this.io = io;
    }

    @Override
    public WnNode enter(WnNode nd) {
        return __eval_obj(nd, true);
    }

    @Override
    public WnNode access(WnNode nd) {
        return __eval_obj(nd, true);
    }

    @Override
    public WnNode view(WnNode nd) {
        return __eval_obj(nd, false);
    }

    @Override
    public WnNode read(WnNode nd) {
        return __eval_obj(nd, true);
    }

    @Override
    public WnNode write(WnNode nd) {
        return __eval_obj(nd, true);
    }

    @Override
    public WnNode remove(WnNode nd) {
        return __eval_obj(nd, false);
    }

    protected WnObj __eval_obj(WnNode nd, boolean auto_unlink) {
        WnObj o = io.toObj(nd);
        // 处理链接文件
        if (auto_unlink && o.isLink()) {
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
                    WnObj p = io.getParent(o);
                    o = io.fetch(p, ln);
                }
            }
            // 如果节点不存在
            if (null == o)
                throw Er.create("e.io.obj.noexists", ln);

            o.path(nd.path());
            o.name(nd.name());
        }
        return o;
    }
}
