package org.nutz.walnut.ext.job.hdl;

import org.nutz.walnut.ext.job.WnJobService;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

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
