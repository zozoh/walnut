package org.nutz.walnut.impl.box.cmd;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.usr.WnRole;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.impl.io.WnEvalLink;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;
import org.nutz.web.WebException;

public class cmd_grp extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        // 分析参数
        ZParams params = ZParams.parse(args, "cqnbisho", "^(json|quiet)$");

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
        String grp = params.val(0);

        // 添加到组
        if (willAppend) {
            String unm = __get_unm(params, "a", myName);

            // 检查权限
            __check_right(sys, me, grp, unm);

            int role = __check_role(params);

            WnUsr u = sys.usrService.check(unm);
            sys.usrService.setRoleInGroup(u, grp, role);
        }
        // 从组中删除
        else if (willDelete) {
            String unm = __get_unm(params, "d", myName);

            // 检查权限
            __check_right(sys, me, grp, unm);

            WnUsr u = sys.usrService.check(unm);
            sys.usrService.removeRoleFromGroup(u, grp);
        }
        // 显示指定组内部所有的用户
        else if (!Strings.isBlank(grp)) {

            // 检查权限
            __check_right(sys, me, grp, null);

            // 查询
            final List<WnRole> list = new LinkedList<WnRole>();
            sys.usrService.eachInGroup(grp, null, (int index, WnRole r, int len) -> {
                WnUsr u = sys.usrService.fetch("id:" + r.usr);
                if (null != u) {
                    r.usr = u.name();
                    list.add(r);
                }
            });

            // 输出 JSON
            if (params.is("json")) {
                JsonFormat jfmt = Cmds.gen_json_format(params);
                sys.out.println(Json.toJson(list, jfmt));
            }
            // 输出成表格
            else {
                params.setv("t", "usr,roleName,role");

                List<NutMap> outs = new ArrayList<NutMap>(list.size());
                for (WnRole r : list) {
                    NutMap map = new NutMap();
                    map.put("usr", r.usr);
                    map.put("roleName", r.roleName);
                    map.put("role", r.role);
                    outs.add(map);
                }
                Cmds.output_objs_as_table(sys, params, null, outs);
            }
        }
        // 默认显示指定用户所在的组
        else {
            String unm = __get_unm(params, "get", myName);

            // 检查权限
            __check_right(sys, me, null, unm);

            WnUsr u = sys.usrService.check(unm);

            this.__show_usr_group(sys, params, u);

        }

    }

    private void __check_right(WnSystem sys, WnUsr me, String grp, String unm) {
        // 那么本组的管理员可以进行这个操作
        int _ro = Strings.isBlank(grp) ? -10000 : sys.usrService.getRoleInGroup(me, grp);
        if (Wn.ROLE.ADMIN != _ro) {

            // 如果不是本组管理员，根用户成员也成
            _ro = sys.usrService.getRoleInGroup(me, "root");
            if (_ro != Wn.ROLE.ADMIN && _ro != Wn.ROLE.MEMBER) {
                // 靠，木权限
                throw Er.create("e.me.nopvg");
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
        String grp = params.val(0);
        // 得到 JSON 格式化配置
        JsonFormat jfmt = Cmds.gen_json_format(params);
        // 展示用户在组里的角色
        if (null != grp) {
            WnRole r = new WnRole();
            r.grp = grp;
            r.usr = u.name();
            r.role = sys.usrService.getRoleInGroup(u, r.grp);
            r.roleName = Wn.ROLE.getRoleName(r.role);
            if (params.is("json")) {
                sys.out.println(Json.toJson(r, jfmt));
            } else {
                sys.out.printlnf("%s %s", r.role, r.roleName);
            }
        }
        // 显示用户都属于哪些组
        else {
            List<String> list = sys.usrService.findMyGroups(u);
            // 查询角色
            List<WnRole> list2 = new ArrayList<>(list.size());
            for (String g : list) {
                WnRole r = new WnRole();
                r.grp = g;
                r.usr = u.name();
                r.role = sys.usrService.getRoleInGroup(u, r.grp);
                r.roleName = Wn.ROLE.getRoleName(r.role);
                list2.add(r);
            }
            // 输出
            if (params.is("json")) {
                sys.out.println(Json.toJson(list2, jfmt));
            }
            // 输出格式化信息
            else {
                for (WnRole r : list2) {
                    sys.out.printlnf("%-8s %2d %s", r.grp, r.role, r.roleName);
                }
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

}
