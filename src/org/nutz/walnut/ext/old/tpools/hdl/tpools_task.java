package org.nutz.walnut.ext.old.tpools.hdl;

import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.old.tpools.MyPools;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnRun;

public class tpools_task implements JvmHdl {

    private static final Log log = Logs.get();

    protected MyPools pools;

    protected WnRun wnRun;

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        if (pools == null)
            pools = hc.ioc.get(MyPools.class);
        if (wnRun == null)
            wnRun = hc.ioc.get(WnRun.class);
        String name = hc.params.val_check(0);
        String cmd = hc.params.val_check(1);
        WnObj dst;
        if (hc.params.has("out")) {
            String outpath = Wn.normalizeFullPath(hc.params.get("out"), sys);
            dst = sys.io.createIfNoExists(null, outpath, WnRace.FILE);
        } else
            dst = null;

        ThreadPoolExecutor es = pools.getOrCreate(name, 0);
        Future<?> fu = (Future<?>) es.submit(new Runnable() {
            public void run() {
                try {
                    Wn.WC().setSession(sys.session.clone());
                    String out = wnRun.exec("pools-" + name, sys.getMyName(), cmd);
                    if (dst != null)
                        sys.io.writeText(dst, out);
                }
                catch (Throwable e) {
                    sys.out.print("something happen" + e.getMessage());
                    log.info("fuck", e);
                }
            }
        });
        if (hc.params.has("wait")) {
            fu.wait(hc.params.getLong("wait"));
        }
    }

}
