package org.nutz.walnut.ext.www.hdl;

import org.nutz.json.Json;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.auth.WnAuthSession;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.www.impl.WnWebService;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;

@JvmHdlParamArgs("cqn")
public class www_pvg implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 得到当前用户
        WnAccount me = sys.getMe();

        // 权限表
        NutMap myAvaPvg = new NutMap();

        // 看看是否需要读取
        if (WnAuthSession.V_BT_AUTH_BY_DOMAIN.equals(sys.session.getByType())) {
            String siteId = sys.session.getVars().getString(WnAuthSession.V_WWW_SITE_ID);
            WnObj oWWW = sys.io.get(siteId);
            if (null != oWWW) {
                WnWebService webs = new WnWebService(sys, oWWW);
                if (me.hasRoleName()) {
                    NutBean pvg = webs.getSite().readRoleAsJson(me.getRoleName());
                    if (null != pvg) {
                        myAvaPvg.putAll(pvg);
                    }
                }
            }
        }

        // 输出
        String json = Json.toJson(myAvaPvg, hc.jfmt);
        sys.out.println(json);
    }

}
