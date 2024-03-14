package com.site0.walnut.impl.box.cmd;

import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;

public class cmd_whoami extends JvmExecutor {

    public void exec(WnSystem sys, String[] args) throws Exception {
        sys.out.print(sys.getMe().getName());
    }

}
