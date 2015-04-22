package org.nutz.walnut.impl.security;

import org.nutz.lang.Maths;
import org.nutz.lang.util.Callback;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnIndexer;
import org.nutz.walnut.api.io.WnNode;
import org.nutz.walnut.api.io.WnObj;

public class WnSecurity implements Callback<WnNode> {

    private WnIndexer indexer;

    private WnObj last;

    private Callback<WnObj> callback;

    private int modeMask;

    public WnSecurity(WnIndexer indexer, int mm) {
        this.indexer = indexer;
        this.modeMask = mm;
    }

    public WnSecurity callback(Callback<WnObj> callback) {
        this.callback = callback;
        return this;
    }

    @Override
    public void invoke(WnNode nd) {
        last = indexer.get(nd.id()).setNode(nd);
        
        // // 检查基本权限
        // int mode = last.mode();
        //
        // // 检查 other
        // int m = mode & 7;
        // if (!Maths.isMaskAll(m, mode))
        // throw Er.create("e.io.forbiden", nd);
        //
        // // 检查 member
        // m = mode >> 3 & 7;
        // if (!Maths.isMaskAll(m, mode))
        // throw Er.create("e.io.forbiden", nd);
        //
        // // 检查 admin
        // m = mode >> 6 & 7;
        // if (!Maths.isMaskAll(m, mode))
        // throw Er.create("e.io.forbiden", nd);

        //
        if (null != callback) {
            callback.invoke(last);
        }
    }

    public WnObj getLastObj() {
        return last;
    }

}
