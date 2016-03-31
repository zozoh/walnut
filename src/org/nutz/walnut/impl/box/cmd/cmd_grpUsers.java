package org.nutz.walnut.impl.box.cmd;

import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.usr.WnRole;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.impl.io.WnEvalLink;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnPager;
import org.nutz.walnut.util.ZParams;

public class cmd_grpUsers extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {

        // 分析参数
        ZParams params = ZParams.parse(args, "iocnqhbslAVNPHQ");

        // 所有逻辑都是在不检查安全性的前提下进行
        Wn.WC().security(new WnEvalLink(sys.io), () -> {
            __exec_without_security(sys, params);
        });

    }

    private void __exec_without_security(WnSystem sys, ZParams params) {
        // 得到要操作的用户
        String myName = sys.se.me();
        WnUsr me = sys.usrService.check(myName);

        // 必须是组的管理员，或者是根组用户
        String grp = params.vals.length > 0 ? params.vals[0] : sys.me.mainGroup();
        if (!__I_am_admin_of_group(sys, me, grp)) {
            // 如果要列 root 组，那么必须管理员才能干
            if ("root".equals(grp))
                throw Er.create("e.me.nopvg");
            __assert_I_am_root_member(sys, me);
        }

        // 分析过滤条件
        NutMap mamap;
        String match = params.get("match");

        // 从参数中读取过滤条件
        if (!Strings.isBlank(match)) {
            mamap = Lang.map(match);
        }
        // 没条件 ..
        else {
            mamap = new NutMap();
        }

        // 将 role 从参数里分离
        int role = mamap.getInt("role", 0);
        mamap.remove("role");

        // 得到翻页器
        WnPager wp = new WnPager(params);

        // 准备结果列表
        List<WnUsr> list = new LinkedList<WnUsr>();

        // 如果有过滤条件的话，从用户里找
        if (mamap.size() > 0) {
            WnQuery q = new WnQuery();
            q.setAll(mamap);

            // 确保查询的是组
            q.setv("my_grps", grp);

            // 记录翻页信息
            if (wp.limit > 0) {
                q.limit(wp.limit);
                q.skip(wp.skip);
            }
            if (role > 0) {
                q.setv("role", role);
            }
            // 查吧
            sys.usrService.each(q, (int index, WnUsr u, int len) -> {
                // 去掉不想给看的字段
                __rm_hidden_key(u);

                // 加入列表
                list.add(u);
            });
        }
        // 没有的话，从组里找
        else {
            WnQuery q = new WnQuery();
            // 记录翻页信息
            if (wp.limit > 0) {
                q.limit(wp.limit);
                q.skip(wp.skip);
            }
            if (role > 0) {
                q.setv("role", role);
            }
            // 查吧
            sys.usrService.eachInGroup(grp, q, (int index, WnRole r, int len) -> {
                WnUsr u = sys.usrService.fetch("id:" + r.usr);
                if (null != u) {
                    // 去掉不想给看的字段
                    __rm_hidden_key(u);

                    // 加入列表
                    list.add(u);
                }
            });
        }

        // 最后输出，肯定要输出成列表
        params.setv("l", true);
        output_objs(sys, params, wp, list, false);
    }

    private void __rm_hidden_key(WnUsr u) {
        u.remove("passwd");
        u.remove("salt");
        u.remove("pid");
        u.remove("ph");
        u.remove("mime");
        u.remove("pid");
        u.remove("c");
        u.remove("g");
        u.remove("m");
        u.remove("d0");
        u.remove("d1");
        u.remove("md");
        u.remove("my_grps");
        u.remove("len");
        u.remove("race");
        u.remove("data");
        u.remove("sha1");
    }

    private void __assert_I_am_root_member(WnSystem sys, WnUsr me) {
        int roleInRoot = sys.usrService.getRoleInGroup(me, "root");
        if (!(roleInRoot == 1 || roleInRoot == 10)) {
            throw Er.create("e.me.nopvg");
        }
    }

    private boolean __I_am_admin_of_group(WnSystem sys, WnUsr me, String grp) {
        return sys.usrService.getRoleInGroup(me, grp) == 1;
    }
}
