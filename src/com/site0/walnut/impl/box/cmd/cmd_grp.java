package com.site0.walnut.impl.box.cmd;

import java.util.ArrayList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutBean;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.login.WnRole;
import com.site0.walnut.login.WnRoleList;
import com.site0.walnut.login.WnRoleType;
import com.site0.walnut.login.WnUser;
import com.site0.walnut.login.role.WnRoles;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.ZParams;
import org.nutz.web.WebException;

public class cmd_grp extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        // 分析参数
        ZParams params = ZParams.parse(args, "cqnbisho", "^(json|quiet)$");

        // 所有逻辑都是在不检查安全性的前提下进行
        try {
            __exec_without_security(sys, params);
        }
        // 看看是否需要抛出错误
        catch (WebException e) {
            if (!params.is("quiet")) {
                throw e;
            }
        }
    }

    private void __exec_without_security(WnSystem sys, ZParams params) {

        boolean willAppend = params.has("a");
        boolean willDelete = params.has("d");

        // 得到要操作的用户
        WnUser me = sys.getMe();
        String myName = sys.getMyName();
        String grp = params.val(0);

        // 添加到组
        if (willAppend) {
            String unm = __get_unm(params, "a", myName);

            // 检查权限
            __check_right(sys, me, grp, unm);

            WnRoleType role = __check_role(params);

            WnUser u = sys.auth.checkUser(unm);
            sys.auth.addRole(u, grp, role);
        }
        // 从组中删除
        else if (willDelete) {
            String unm = __get_unm(params, "d", myName);

            // 检查权限
            __check_right(sys, me, grp, unm);

            WnUser u = sys.auth.checkUser(unm);
            sys.auth.removeRole(u.getName(), grp);
        }
        // 显示指定组内部所有的用户
        else if (!Strings.isBlank(grp)) {

            // 检查权限
            __check_right(sys, me, grp, null);

            // 查询
            WnRoleList list = sys.auth.queryRolesOf(grp);
            List<NutBean> outs = list.toBeans();

            // 输出 JSON
            if (params.is("json")) {
                JsonFormat jfmt = Cmds.gen_json_format(params);
                sys.out.println(Json.toJson(outs, jfmt));
            }
            // 输出成表格
            else {
                String[] keys = Wlang.array("grp", "unm", "type", "role");
                params.setv("t", Strings.join(",", keys));
                Cmds.output_objs_as_table(sys, params, null, outs);
            }
        }
        // 默认显示指定用户所在的组
        else {
            String unm = __get_unm(params, "get", myName);

            // 检查权限
            __check_right(sys, me, sys.getMyGroup(), unm);

            WnUser u = sys.auth.checkUser(unm);

            // 查询
            WnRoleList roles = sys.auth.getRoles(u);

            // 输出 JSON
            if (params.is("json")) {
                List<NutBean> outs = new ArrayList<>(roles.size());
                for (WnRole role : roles) {
                    outs.add(role.toBean());
                }
                JsonFormat jfmt = Cmds.gen_json_format(params);
                sys.out.println(Json.toJson(outs, jfmt));
            }
            // 输出成表格
            else {
                String[] keys = Wlang.array("grp", "type", "role");
                params.setv("t", Strings.join(",", keys));

                List<NutBean> outs = new ArrayList<>(roles.size());
                for (WnRole role : roles) {
                    outs.add(role.toBean());
                }
                Cmds.output_objs_as_table(sys, params, null, outs);
            }

        }

    }

    private void __check_right(WnSystem sys, WnUser me, String grp, String unm) {
        WnRoleList roles = sys.auth.getRoles(me);
        // 那么本组的管理员可以进行这个操作
        if (!roles.isAdminOfRole(grp)) {
            // 如果不是本组管理员，根用户成员也成
            if (!roles.isMemberOfRole("root")) {
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

    private WnRoleType __check_role(ZParams params) {
        int role = params.getInt("role", WnRoleType.MEMBER.getValue());
        return WnRoles.fromInt(role);
    }

}
