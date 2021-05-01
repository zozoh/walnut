package org.nutz.walnut.ext.old.job.hdl;

import org.nutz.walnut.ext.old.job.WnJobService;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

public class job_stop implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        if (!WnJobService.me.isRunning()) {
            sys.out.println("job service isn't running");
            return;
        }
        WnJobService.me.depose();
        return;
    }

}
