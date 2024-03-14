package com.site0.walnut.impl.box.cmd;

import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;

public class cmd_ioc extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        for (String nm : ioc.getNames()) {
            sys.out.println(nm);
        }
    }

}
