package org.nutz.walnut.impl.box.cmd;

import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;

public class cmd_whoami extends JvmExecutor {

    public void exec(WnSystem sys, String[] args) throws Exception {
        sys.out.print(sys.me.name());
    }

}
