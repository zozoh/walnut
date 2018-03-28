package org.nutz.walnut.ext.job.hdl;

import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.job.WnJobService;

public class job_stop implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        if (!sys.se.me().equals("root")) {
            sys.err.println("only root can stop job service");
            return;
        }
        if (!WnJobService.me.isRunning()) {
            sys.out.println("job service isn't running");
            return;
        }
        WnJobService.me.depose();
        return;
    }

}
