package org.nutz.walnut.ext.ftpd;

import org.nutz.trans.Atom;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.impl.box.JvmHdlExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

public class cmd_ftpd extends JvmHdlExecutor {

    public void exec(WnSystem sys, String[] args) throws Exception {
        if (args.length > 0 && !"passwd".equals(args[0])) {
            // 检查权限: root 组管理员才能操作
            sys.nosecurity(new Atom() {
                public void run() {
                    if (!Wn.WC().isAdminOf(sys.usrService, "root")) {
                        throw Er.create("e.cmd.ftpd.only_for_root_admin");
                    }
                }
            });
        }

        // 执行子命令
        super.exec(sys, args);
    }
}