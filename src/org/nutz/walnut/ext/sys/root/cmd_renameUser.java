package org.nutz.walnut.ext.sys.root;

import org.nutz.lang.Stopwatch;
import org.nutz.trans.Atom;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.impl.io.WnEvalLink;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

public class cmd_renameUser extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        // 分析参数
        final ZParams params = ZParams.parse(args, "v");

        // 得到要操作的帐号
        Wn.WC().security(new WnEvalLink(sys.io), new Atom() {
            public void run() {
                __exec_without_security(sys, params);
            }
        });

    }

    private void __exec_without_security(WnSystem sys, ZParams params) {
        String newName = params.val_check(0);
        boolean verb = params.is("v");

        // 得到要操作的用户，以及为其创建会话
        WnAccount me = sys.getMe();
        WnAccount u = me;
        if (params.has("unm")) {
            String unm = params.check("u");
            u = sys.auth.checkAccount(unm);
        }

        // 没必要改名
        if (u.isSameName(newName)) {
            if (verb)
                sys.out.println("same name");
            return;
        }

        // root 用户不能改名
        if (u.isRoot()) {
            throw Er.create("e.cmd.renameUser.root");
        }
        // 不能修改当前用户名称
        if (u.isSame(me)) {
            throw Er.create("e.cmd.renameUser.self");
        }
        // 执行这个命令，需要 root 组的管理员权限
        if (!sys.auth.isAdminOfGroup(me, "root")) {
            throw Er.create("e.cmd.renameUser.nopvg");
        }

        // 执行改名
        sys.out.println("start");
        Stopwatch sw = Stopwatch.begin();

        sys.auth.renameAccount(u, newName);

        sw.stop();
        sys.out.println("All done in : " + sw.toString());

    }

}
