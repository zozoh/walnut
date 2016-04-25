package org.nutz.walnut.impl.box.cmd;

import java.util.LinkedList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.usr.WnRole;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.TextTable;
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

        boolean willAppend = params.has("a");
        boolean willDelete = params.has("d");

        // 得到要操作的用户
        String myName = sys.se.me();
        WnUsr me = sys.usrService.check(myName);
        String grp = params.vals.length > 0 ? params.vals[0] : myName;

        // 如果要操作的用户不是自己，那么必须得有 root 组的权限
        int roleInRoot = sys.usrService.getRoleInGroup(me, "root");
        boolean I_am_admin_of_root = roleInRoot == Wn.ROLE.ADMIN;
        boolean I_am_member_of_root = I_am_admin_of_root || roleInRoot == Wn.ROLE.MEMBER;

        // 添加到组
        if (willAppend) {
            String unm = __get_unm(params, "a", myName);

            if (!myName.equals(unm) && !I_am_member_of_root) {
                throw Er.create("e.me.nopvg");
            }

            if ("root".equals(grp) && !I_am_admin_of_root)
                throw Er.create("e.me.nopvg");

            if (!I_am_member_of_root)
                __assert_I_am_admin_of_group(sys, me, grp);

            int role = __check_role(params);

            WnUsr u = sys.usrService.check(unm);
            sys.usrService.setRoleInGroup(u, grp, role);
        }
        // 从组中删除
        else if (willDelete) {
            String unm = __get_unm(params, "d", myName);

            if (!myName.equals(unm) && !I_am_member_of_root) {
                throw Er.create("e.me.nopvg");
            }

            if ("root".equals(grp) && !I_am_admin_of_root)
                throw Er.create("e.me.nopvg");

            if (!I_am_member_of_root)
                __assert_I_am_admin_of_group(sys, me, grp);

            WnUsr u = sys.usrService.check(unm);
            sys.usrService.removeRoleFromGroup(u, grp);
        }
        // 显示指定用户所在的组
        else if (params.has("get")) {
            String unm = __get_unm(params, "get", myName);

            if (!myName.equals(unm) && !I_am_member_of_root) {
                throw Er.create("e.me.nopvg");
            }

            WnUsr u = sys.usrService.check(unm);

            this.__show_usr_group(sys, params, u);

        }
        // 显示指定组内部所有的用户
        else {
            final List<WnRole> list = new LinkedList<WnRole>();
            sys.usrService.eachInGroup(grp, null, (int index, WnRole r, int len) -> {
                WnUsr u = sys.usrService.fetch("id:" + r.usr);
                if (null != u) {
                    r.usr = u.name();
                    list.add(r);
                }
            });

            if (params.is("json")) {
                sys.out.println(Json.toJson(list, JsonFormat.nice()));
            } else {
                TextTable tt = new TextTable(4);
                for (WnRole r : list)
                    tt.addRow(Lang.array(r.grp, r.usr, r.roleName, "" + r.role));
                sys.out.print(tt.toString());
            }
        }

    }

    private String __get_unm(ZParams params, String key, String myName) {
        String unm = params.getString(key);

        if (Strings.isBlank(unm))
            unm = myName;
        return unm;
    }

    private void __show_usr_group(WnSystem sys, ZParams params, WnUsr u) {
        // 展示用户在组里的角色
        if (params.vals.length > 0) {
            WnRole r = new WnRole();
            r.grp = params.vals[0];
            r.usr = u.name();
            r.role = sys.usrService.getRoleInGroup(u, r.grp);
            r.roleName = Wn.ROLE.getRoleName(r.role);
            if (params.is("json")) {
                sys.out.println(Json.toJson(r, JsonFormat.nice()));
            } else {
                sys.out.printlnf("%s %s", r.role, r.roleName);
            }
        }
        // 显示用户都属于哪些组
        else {
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
