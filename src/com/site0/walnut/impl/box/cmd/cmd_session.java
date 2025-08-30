package com.site0.walnut.impl.box.cmd;

import java.util.ArrayList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.login.WnLoginApi;
import com.site0.walnut.login.role.WnRoleType;
import com.site0.walnut.login.session.WnSession;
import com.site0.walnut.login.site.WnLoginSite;
import com.site0.walnut.login.usr.WnUser;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

public class cmd_session extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, "cqnbish", "^(list)$");
        JsonFormat jfmt = Cmds.gen_json_format(params);

        int limit = params.getInt("limit", 50);
        int skip = params.getInt("skip", 0);
        WnQuery q = new WnQuery();
        q.limit(limit).skip(skip);
        // 设置了条件
        if (params.vals.length > 0) {
            for (String val : params.vals) {
                NutMap map = Wlang.map(val);
                q.add(map);
            }
        }
        // 设置了排序
        if (params.has("sort")) {
            NutMap sort = params.getMap("sort");
            if (null != sort && !sort.isEmpty()) {
                q.sort(sort);
            }
        }

        // 列出指定站点的的会话
        if (params.has("site") || params.has("host") || params.is("list")) {
            List<NutBean> beans;
            // 系统会话列表，只有管理员能干
            if (params.is("list")) {
                beans = __load_as_sys(sys, q);
            }
            // 站点会话列表，只有对应站点管理员能干
            else {
                beans = __load_as_site(sys, params, q);
            }

            // 表格输出
            if (params.has("tab")) {
                output_as_table(sys, params, beans);
                return;
            }
            // JSON 输出
            else {
                String json = Json.toJson(beans, jfmt);
                sys.out.println(json);
            }
            return;
        }

        // 仅仅显示当前会话
        NutBean se = sys.session.toBean(sys.auth);
        String json = Json.toJson(se, jfmt);

        // 打印输出
        sys.out.println(json);
    }

    protected List<NutBean> __load_as_sys(WnSystem sys, WnQuery q) {
        List<NutBean> beans;
        WnUser me = sys.getMe();
        WnRoleType myRole = sys.roles().getRoleTypeOfGroup(me, "root");
        if (!myRole.isOf(WnRoleType.ADMIN)) {
            throw Er.create("e.cmd.session.list.forbid",
                            "Should be ADMIN of root");
        }
        List<WnSession> seList = sys.auth.querySession(q);

        // 转换 Session 为普通 Beans
        beans = new ArrayList<>(seList.size());
        for (WnSession se : seList) {
            beans.add(se.toBean(sys.auth));
        }
        return beans;
    }

    protected List<NutBean> __load_as_site(WnSystem sys,
                                           ZParams params,
                                           WnQuery q) {
        String sitePath = params.get("site");
        String hostName = params.get("host");
        String aph = Wn.normalizeFullPath(sitePath, sys);
        WnLoginSite site = WnLoginSite.create(sys.io, aph, hostName);
        WnLoginApi auth = site.auth();

        // 必须是站点管理员
        if (!site.isRoleOfHome(sys, WnRoleType.ADMIN)) {
            throw Er.create("e.cmd.session.list.forbid",
                            "Should be ADMIN of site:" + sitePath);
        }

        // 读取列表
        List<WnSession> seList = auth.querySession(q);

        // 转换 Session 为普通 Beans
        List<NutBean> beans = new ArrayList<>(seList.size());
        for (WnSession se : seList) {
            beans.add(se.toBean(auth));
        }
        return beans;
    }

    private void output_as_table(WnSystem sys,
                                 ZParams params,
                                 List<NutBean> beans) {
        String tab = params.get("tab");
        if ("ALL".equals(tab)) {
            tab = "mainRole,site,type,ticket,"
                  + "loginName,mainGroup,duration,"
                  + "createTime:time,expiAt:time,"
                  + "parentTicket,childTicket";
        }
        String[] cols = Ws.splitIgnoreBlank(tab);
        boolean showBorder = params.is("b");
        boolean showHeader = params.is("h");
        boolean showSummary = params.is("s");
        boolean showIndex = params.is("i");
        int indexBase = params.getInt("ibase", 0);

        // 输出
        Cmds.output_objs_as_table(sys,
                                  null,
                                  beans,
                                  cols,
                                  showBorder,
                                  showHeader,
                                  showSummary,
                                  showIndex,
                                  indexBase);
    }

}
