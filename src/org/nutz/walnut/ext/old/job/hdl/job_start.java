package org.nutz.walnut.ext.old.job.hdl;

import org.nutz.walnut.ext.old.job.WnJobService;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

public class job_start implements JvmHdl {

    public void invoke(WnSystem sys, JvmHdlContext hc) {
        if (!WnJobService.me.isRunning()) {
            WnJobService.me.init();
        } else
            sys.out.println("aready started");
        return;
    }

}
