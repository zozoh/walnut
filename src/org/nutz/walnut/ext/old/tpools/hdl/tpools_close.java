package org.nutz.walnut.ext.old.tpools.hdl;

import java.util.concurrent.ThreadPoolExecutor;

import org.nutz.walnut.ext.old.tpools.MyPools;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

public class tpools_close implements JvmHdl {
    
    protected MyPools pools;

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        if (pools == null)
            pools = hc.ioc.get(MyPools.class);
        String name = hc.params.val_check(0);
        ThreadPoolExecutor es = pools.get(name);
        if (es == null) {
            sys.out.print("no such pool");
        }
        else {
            es.shutdown();
            sys.out.print("done");
        }
    }

}
