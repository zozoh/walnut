package org.nutz.walnut.ext.ai.tfodapi.hdl;

import org.nutz.lang.Lang;
import org.nutz.walnut.ext.ai.tfodapi.cmd_tfodt;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

// 新建一个训练任务
public class tfodt_train_stop implements JvmHdl {

    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        Process process = cmd_tfodt.P.get(hc.params.val(0));
        if (process == null) {
            sys.out.print("not running");
            return;
        }
        if (process.isAlive()) {
            process.destroy();
            Lang.quiteSleep(5000);
            if (process.isAlive())
                process.destroyForcibly();
            sys.out.print("done");
            return;
        }
        else {
            sys.out.print("process was exited");
            cmd_tfodt.P.remove(hc.params.val(0));
        }
    }

}
