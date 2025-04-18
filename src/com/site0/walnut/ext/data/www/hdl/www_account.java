package com.site0.walnut.ext.data.www.hdl;

import java.util.ArrayList;
import java.util.List;

import org.nutz.json.Json;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.auth.WnAccount;
import com.site0.walnut.api.auth.WnAuths;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.ext.data.www.cmd_www;
import com.site0.walnut.ext.data.www.impl.WnWebService;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.WnPager;

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
            list = Wlang.list(u);
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
            q.setv("th_live", "[0,)");
            q.limit(wp.limit).skip(wp.skip);
            q.sort(sort);
            list = webs.getAuthApi().queryAccount(q);
            wp.setupQuery(sys, q);
        }

        // 转换输出列表
        List<NutMap> outputs = new ArrayList<>(list.size());
        for (WnAccount u : list) {
            outputs.add(u.toBean(WnAuths.ABMM.LOGIN
                                 | WnAuths.ABMM.INFO
                                 | WnAuths.ABMM.META
                                 | WnAuths.ABMM.HOME));
        }

        // 准备返回值
        Object reo;

        // 木有东东
        if (outputs.size() == 0 && !asList) {
            reo = null;
        } else if (outputs.size() == 1 && !asList) {
            reo = outputs.get(0);
        }
        // 分页模式
        else if (hc.params.has("limit")) {
            reo = Cmds.createQueryResult(wp, outputs);
        }
        // 列表模式
        else {
            reo = outputs;
        }

        // 修改账户信息
        if (hc.params.has("u")) {
            String json = Cmds.getParamOrPipe(sys, hc.params, "u", true);
            if (!Strings.isBlank(json)) {
                NutMap meta = Wlang.map(json);
                for (WnAccount u : list) {
                    webs.getAuthApi().saveAccount(u, meta);
                }
            }
        }

        // 输出
        cmd_www.outputJsonOrAjax(sys, reo, hc);
    }

}
