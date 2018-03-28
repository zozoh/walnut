package org.nutz.walnut.ext.job.hdl;

import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.job.WnJobService;

public class job_status implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        if (!WnJobService.me.isRunning()) {
            sys.out.println("job service isn't running");
        } else {
            sys.out.printlnf("job service is running\n%s", WnJobService.me);
        }
        return;
    }

}
