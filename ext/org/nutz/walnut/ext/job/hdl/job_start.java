package org.nutz.walnut.ext.job.hdl;

import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.job.WnJob;

public class job_start implements JvmHdl {

    public void invoke(WnSystem sys, JvmHdlContext hc) {
        if (!sys.se.me().equals("root")) {
            sys.err.println("only root can start job service");
            return;
        }
        if (!WnJob.me.isRunning()) {
            WnJob.me.init();
        }
        else
            sys.out.println("aready started");
        return;
    }

}
