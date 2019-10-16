package org.nutz.walnut.ext.pools.hdl;

import org.nutz.json.JsonFormat;
import org.nutz.walnut.ext.pools.MyPools;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

public class pools_list implements JvmHdl {
    
    protected MyPools pools;

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        if (pools == null)
            pools = hc.ioc.get(MyPools.class);
        sys.out.writeJson(pools.names(), JsonFormat.full());
    }

}
