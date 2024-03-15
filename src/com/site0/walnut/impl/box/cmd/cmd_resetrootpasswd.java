package com.site0.walnut.impl.box.cmd;

import com.site0.walnut.util.Wlang;
import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;

public class cmd_resetrootpasswd extends JvmExecutor {

    @Override
    public void exec(final WnSystem sys, String[] args) throws Exception {
        // 暂时先放弃这个命令吧，有点危险的样子 ...
        // WnConfig conf = ioc.get(WnConfig.class, "conf");
        // final String dftPasswd = conf.get("root-init-passwd");
        //
        // if (Strings.isBlank(dftPasswd))
        // throw Er.create("e.cmd.resetrootpasswd.notAvaliable");
        //
        // Wn.WC().security(null, new Atom() {
        // public void run() {
        // sys.usrService.setPassword("root", dftPasswd);
        // }
        // });
        //
        // sys.out.println("root user password reseted!");
        throw Wlang.noImplement();
    }

}
