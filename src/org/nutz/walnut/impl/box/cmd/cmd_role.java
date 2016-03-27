package org.nutz.walnut.impl.box.cmd;

import org.nutz.lang.Strings;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.impl.io.WnEvalLink;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

public class cmd_role extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) {

        ZParams params = ZParams.parse(args, "^nm$");

        if (params.vals.length == 0) {
            throw Er.create("e.cmd.lackargs");
        }

        Wn.WC().security(new WnEvalLink(sys.io), () -> {
            __exec_without_security(sys, params);
        });

    }

    private void __exec_without_security(WnSystem sys, ZParams params) {
        // 分析输入参数
        String str = params.vals[0];
        String[] ss = Strings.splitIgnoreBlank(str, ":");
        String unm, grp;
        if (ss.length > 1) {
            unm = ss[0];
            grp = ss[1];
        } else {
            unm = sys.me.name();
            grp = ss[0];
        }

        // 得到要操作的用户
        String myName = sys.se.me();
        WnUsr me = sys.usrService.check(myName);
        WnUsr u = myName.equals(unm) ? me : sys.usrService.check(unm);

        // 如果要操作的用户不是自己，那么必须得有 root 组的权限
        int roleInRoot = sys.usrService.getRoleInGroup(me, "root");
        boolean I_am_member_of_root = roleInRoot == 1 || roleInRoot == 10;
        if (!myName.equals(unm) && !I_am_member_of_root) {
            throw Er.create("e.me.nopvg");
        }

        // 改变权限
        if (params.has("role")) {
            int role = params.getInt("role");
            // 检查合法性
            Wn.ROLE.getRoleName(role);

            if (!I_am_member_of_root)
                __assert_I_am_admin_of_group(sys, params, me, grp);

            // 修改
            sys.usrService.setRoleInGroup(u, grp, role);
        }
        // 显示权限
        else {
            int role = sys.usrService.getRoleInGroup(u, grp);
            if (params.is("nm")) {
                sys.out.printlnf("%d : %s", role, Wn.ROLE.getRoleName(role));
            } else {
                sys.out.println("" + role);
            }
        }
    }

    private void __assert_I_am_admin_of_group(WnSystem sys, ZParams params, WnUsr me, String grp) {
        int role = sys.usrService.getRoleInGroup(me, grp);
        if (role != 1) {
            throw Er.create("e.me.nopvg");
        }
    }
}
