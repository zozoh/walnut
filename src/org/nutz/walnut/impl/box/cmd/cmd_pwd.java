package org.nutz.walnut.impl.box.cmd;

import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;

public class cmd_pwd extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) {
        String pwd = sys.se.envs().getString("PWD");
        sys.out.writeLine(pwd);
    }

}
