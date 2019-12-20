package org.nutz.walnut.ext.www.hdl;

import java.util.List;

import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.ext.www.cmd_www;
import org.nutz.walnut.ext.www.impl.WnWebService;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnPager;

@JvmHdlParamArgs(value = "cqn", regex = "^(ajax|list)$")
public class www_account implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // -------------------------------
        // 站点/票据
        String site = hc.params.val(0);
        String user = hc.params.val(1);
        boolean asList = hc.params.is("list");
        WnPager wp = new WnPager(hc.params);

        // -------------------------------
        // 准备服务类
        WnObj oWWW = Wn.checkObj(sys, site);
        WnWebService webs = new WnWebService(sys, oWWW);

        // 准备结果列表
        List<WnAccount> list;

        // 作为唯一标识
        if (!Strings.isBlank(user) && !Strings.isQuoteBy(user, '{', '}')) {
            WnAccount u = webs.getAuthApi().checkAccount(user);
            list = Lang.list(u);
        }
        // 否则作为查询条件
        else {
            NutMap sort = null;
            if (hc.params.has("sort")) {
                sort = hc.params.getMap("sort");
            }
            WnQuery q = new WnQuery();
            if (!Strings.isBlank(user)) {
                NutMap map = Json.fromJson(NutMap.class, user);
                if (null != map && !map.isEmpty()) {
                    q.setAll(map);
                }
            }
            q.limit(wp.limit).skip(wp.skip);
            q.sort(sort);
            list = webs.getAuthApi().queryAccount(q);
            wp.setupQuery(sys, q);

        }

        // 准备返回值
        Object reo;

        // 木有东东
        if (list.size() == 0 && !asList) {
            reo = null;
        } else if (list.size() == 1 && !asList) {
            reo = list.get(0);
        }
        // 分页模式
        else if (hc.params.has("limit")) {
            reo = Cmds.createQueryResult(wp, list);
        }
        // 列表模式
        else {
            reo = list;
        }

        // 修改账户信息
        if (hc.params.has("u")) {
            String json = Cmds.getParamOrPipe(sys, hc.params, "u", true);
            if (!Strings.isBlank(json)) {
                NutMap meta = Lang.map(json);
                for (WnAccount u : list) {
                    webs.getAuthApi().saveAccount(u, meta);
                }
            }
        }

        // 输出
        cmd_www.outputJsonOrAjax(sys, reo, hc);
    }

}
