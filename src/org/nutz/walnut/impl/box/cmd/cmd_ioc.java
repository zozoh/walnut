package org.nutz.walnut.impl.box.cmd;

import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;

public class cmd_ioc extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        for (String nm : ioc.getNames()) {
            sys.out.println(nm);
        }
    }

}
