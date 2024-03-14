package com.site0.walnut.ext.net.ftpd;

import org.nutz.trans.Atom;
import com.site0.walnut.api.auth.WnAccount;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.impl.box.JvmHdlExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;

public class cmd_ftpd extends JvmHdlExecutor {

    public void exec(WnSystem sys, String[] args) throws Exception {
        if (args.length > 0 && !"passwd".equals(args[0])) {
            // 检查权限: root 组管理员才能操作
            sys.nosecurity(new Atom() {
                public void run() {
                    WnAccount me = Wn.WC().getMe();
                    if (!sys.auth.isAdminOfGroup(me, "root")) {
                        throw Er.create("e.cmd.ftpd.only_for_root_admin");
                    }
                }
            });
        }

        // 执行子命令
        super.exec(sys, args);
    }
}
