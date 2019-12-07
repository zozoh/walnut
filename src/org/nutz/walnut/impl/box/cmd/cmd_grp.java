package org.nutz.walnut.impl.box.cmd;

import java.util.ArrayList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.auth.WnGroupAccount;
import org.nutz.walnut.api.auth.WnGroupRole;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.ZParams;
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
        WnAccount me = sys.getMe();
        String myName = sys.getMyName();
        String grp = params.val(0);

        // 添加到组
        if (willAppend) {
            String unm = __get_unm(params, "a", myName);

            // 检查权限
            __check_right(sys, me, grp, unm);

            WnGroupRole role = __check_role(params);

            WnAccount u = sys.auth.checkAccount(unm);
            sys.auth.setGroupRole(u, grp, role);
        }
        // 从组中删除
        else if (willDelete) {
            String unm = __get_unm(params, "d", myName);

            // 检查权限
            __check_right(sys, me, grp, unm);

            WnAccount u = sys.auth.checkAccount(unm);
            sys.auth.removeGroupRole(u, grp);
        }
        // 显示指定组内部所有的用户
        else if (!Strings.isBlank(grp)) {

            // 检查权限
            __check_right(sys, me, grp, null);

            // 查询
            List<WnGroupAccount> list = sys.auth.getAccounts(grp);

            // 输出 JSON
            if (params.is("json")) {
                List<NutMap> outs = new ArrayList<>(list.size());
                for (WnGroupAccount wga : list) {
                    outs.add(wga.toBean());
                }
                JsonFormat jfmt = Cmds.gen_json_format(params);
                sys.out.println(Json.toJson(outs, jfmt));
            }
            // 输出成表格
            else {
                String[] keys = Lang.array("unm", "roleName", "role");
                params.setv("t", Strings.join(",", keys));

                List<NutMap> outs = new ArrayList<NutMap>(list.size());
                for (WnGroupAccount wga : list) {
                    outs.add(wga.toBean(keys));
                }
                Cmds.output_objs_as_table(sys, params, null, outs);
            }
        }
        // 默认显示指定用户所在的组
        else {
            String unm = __get_unm(params, "get", myName);

            // 检查权限
            __check_right(sys, me, sys.getMyGroup(), unm);

            WnAccount u = sys.auth.checkAccount(unm);

            // 查询
            List<WnGroupAccount> list = sys.auth.getGroups(u);

            // 输出 JSON
            if (params.is("json")) {
                List<NutMap> outs = new ArrayList<>(list.size());
                for (WnGroupAccount wga : list) {
                    outs.add(wga.toBean());
                }
                JsonFormat jfmt = Cmds.gen_json_format(params);
                sys.out.println(Json.toJson(outs, jfmt));
            }
            // 输出成表格
            else {
                String[] keys = Lang.array("grp", "roleName", "role");
                params.setv("t", Strings.join(",", keys));

                List<NutMap> outs = new ArrayList<NutMap>(list.size());
                for (WnGroupAccount wga : list) {
                    outs.add(wga.toBean(keys));
                }
                Cmds.output_objs_as_table(sys, params, null, outs);
            }

        }

    }

    private void __check_right(WnSystem sys, WnAccount me, String grp, String unm) {
        // 那么本组的管理员可以进行这个操作
        if (!sys.auth.isAdminOfGroup(me, grp)) {
            // 如果不是本组管理员，根用户成员也成
            if (!sys.auth.isMemberOfGroup(me, "root")) {
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

    private WnGroupRole __check_role(ZParams params) {
        int role = params.getInt("role", WnGroupRole.MEMBER.getValue());
        return WnGroupRole.parseInt(role);
    }

}
