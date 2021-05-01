package org.nutz.walnut.ext.old.job;

import java.util.HashSet;
import java.util.Set;

import org.nutz.trans.Atom;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.impl.box.JvmHdlExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

public class cmd_job extends JvmHdlExecutor {

    public static Set<String> limitedCmd = new HashSet<>();
    static {
        limitedCmd.add("start");
        limitedCmd.add("stop");
        limitedCmd.add("status");
    }

    public void exec(WnSystem sys, String[] args) throws Exception {
        if (args.length > 0 && limitedCmd.contains(args[0])) {
            // 检查权限: root/job 组管理员才能操作
            sys.nosecurity(new Atom() {
                public void run() {
                    WnAccount me = Wn.WC().getMe();
                    if (!sys.auth.isAdminOfGroup(me, "root", "job")) {
                        throw Er.create("e.cmd.job.only_for_job_admin");
                    }
                }
            });
        }

        // 执行子命令
        super.exec(sys, args);
    }
}
