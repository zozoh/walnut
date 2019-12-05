package org.nutz.walnut.ext.sshd;

import org.nutz.trans.Atom;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.impl.box.JvmHdlExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

public class cmd_sshd extends JvmHdlExecutor {

    public void exec(WnSystem sys, String[] args) throws Exception {
        if (args.length > 0 && !"passwd".equals(args[0])) {
            // 检查权限: root 组管理员才能操作
            sys.nosecurity(new Atom() {
                public void run() {
                    WnAccount me = Wn.WC().getMe();
                    if (!sys.auth.isAdminOfGroup(me, "root")) {
                        throw Er.create("e.cmd.sshd.only_for_root_admin");
                    }
                }
            });
        }

        // 执行子命令
        super.exec(sys, args);
    }
}
