package org.nutz.walnut.ext.mgadmin;

import org.nutz.walnut.impl.box.JvmHdlExecutor;
import org.nutz.walnut.impl.box.WnSystem;

public class cmd_mgadmin extends JvmHdlExecutor {

    public void exec(WnSystem sys, String[] args) throws Exception {
        if (!"root".equals(sys.me.name())) {
            sys.err.println("e.cmd.mgadmin.only_for_root");
            return;
        }
        super.exec(sys, args);
    }
}
