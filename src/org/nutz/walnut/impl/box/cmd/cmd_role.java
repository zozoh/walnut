package org.nutz.walnut.impl.box.cmd;

import org.nutz.lang.Strings;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

public class cmd_role extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) {

        ZParams params = ZParams.parse(args, "^nm$");

        if (params.vals.length == 0) {
            throw Er.create("e.cmd.lackargs");
        }

        // 分析
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

        // 检查用户
        WnUsr u = sys.usrService.check(unm);

        // 改变权限
        if (params.has("role")) {
            int role = params.getInt("role");
            // 检查合法性
            switch (role) {
            case Wn.ROLE.ADMIN:
            case Wn.ROLE.BLOCK:
            case Wn.ROLE.MEMBER:
            case Wn.ROLE.OTHERS:
            case Wn.ROLE.REQUEST:
                break;
            default:
                throw Er.create("e.cmd.role.invalid", role);
            }

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

}
