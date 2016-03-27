package org.nutz.walnut.impl.box.cmd;

import java.util.List;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Lang;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.impl.io.WnEvalLink;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;
import org.nutz.web.WebException;

public class cmd_grp extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        // 分析参数
        ZParams params = ZParams.parse(args, "^(json|o|quiet)$");

        // 所有逻辑都是在不检查安全性的前提下进行
        Wn.WC().security(new WnEvalLink(sys.io), () -> {
            try {
                __exec_without_security(sys, params);
            }
            // 看看是否需要抛出错误
            catch (WebException e) {
                if (!params.is("quiet")) {
                    throw e;
                }
            }
        });
    }

    private void __exec_without_security(WnSystem sys, ZParams params) {

        boolean showUsrGroups = params.is("o");

        // 得到要操作的用户
        String myName = sys.se.me();
        WnUsr me = sys.usrService.check(myName);
        String unm = params.vals.length > 0 ? params.vals[0] : myName;
        WnUsr u = myName.equals(unm) ? me : sys.usrService.check(unm);

        // 如果要操作的用户不是自己，那么必须得有 root 组的权限
        int roleInRoot = sys.usrService.getRoleInGroup(me, "root");
        boolean I_am_admin_of_root = roleInRoot == Wn.ROLE.ADMIN;
        boolean I_am_member_of_root = I_am_admin_of_root || roleInRoot == Wn.ROLE.MEMBER;
        if (!myName.equals(unm) && !I_am_member_of_root) {
            throw Er.create("e.me.nopvg");
        }

        // 添加到组
        if (params.has("a")) {
            String grp = params.get("a");

            if ("root".equals(grp) && !I_am_admin_of_root)
                throw Er.create("e.me.nopvg");

            if (!I_am_member_of_root)
                __assert_I_am_admin_of_group(sys, me, grp);

            int role = __check_role(params);
            sys.usrService.setRoleInGroup(u, grp, role);
        }
        // 从组中删除
        else if (params.has("d")) {
            String grp = params.get("d");

            if ("root".equals(grp) && !I_am_admin_of_root)
                throw Er.create("e.me.nopvg");

            if (!I_am_member_of_root)
                __assert_I_am_admin_of_group(sys, me, grp);

            sys.usrService.removeRoleFromGroup(u, grp);
        }
        // 都不是，那就必须显示了
        else {
            showUsrGroups = true;
        }

        // 显示用户所在的组
        if (showUsrGroups) {
            List<String> list = sys.usrService.findMyGroups(u);
            if (params.is("json")) {
                sys.out.println(Json.toJson(list, JsonFormat.compact()));
            } else {
                sys.out.println(Lang.concat(" ", list));
            }
        }

    }

    private int __check_role(ZParams params) {
        int role = params.getInt("role", Wn.ROLE.MEMBER);
        // 检查合法性
        Wn.ROLE.getRoleName(role);

        // 返回 role
        return role;
    }

    private void __assert_I_am_admin_of_group(WnSystem sys, WnUsr me, String grp) {
        int role = sys.usrService.getRoleInGroup(me, grp);
        if (role != 1) {
            throw Er.create("e.me.nopvg");
        }
    }

}
