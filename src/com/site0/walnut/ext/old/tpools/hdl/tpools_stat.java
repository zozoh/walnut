package com.site0.walnut.ext.old.tpools.hdl;

import java.util.concurrent.ThreadPoolExecutor;

import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.ext.old.tpools.MyPools;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.WnSystem;

public class tpools_stat implements JvmHdl {
    
    protected MyPools pools;

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        if (pools == null)
            pools = hc.ioc.get(MyPools.class);
        String name = hc.params.val_check(0);
        ThreadPoolExecutor es = pools.get(name);
        NutMap stat = new NutMap();
        if (es == null) {
            stat.put("msg", "no such pool");
        }
        else {
            stat.put("active", es.getActiveCount());
            stat.put("max", es.getMaximumPoolSize());
            stat.put("completed", es.getCompletedTaskCount());
        }
        sys.out.writeJson(stat, JsonFormat.full());
    }

}
