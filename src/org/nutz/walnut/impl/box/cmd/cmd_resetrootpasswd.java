package org.nutz.walnut.impl.box.cmd;

import org.nutz.lang.Strings;
import org.nutz.trans.Atom;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.web.WnConfig;

public class cmd_resetrootpasswd extends JvmExecutor {

    @Override
    public void exec(final WnSystem sys, String[] args) throws Exception {
        WnConfig conf = ioc.get(WnConfig.class, "conf");
        final String dftPasswd = conf.get("root-init-passwd");

        if (Strings.isBlank(dftPasswd))
            throw Er.create("e.cmd.resetrootpasswd.notAvaliable");

        Wn.WC().security(null, new Atom() {
            public void run() {
                sys.usrService.setPassword("root", dftPasswd);
            }
        });

        sys.out.println("root user password reseted!");
    }

}
