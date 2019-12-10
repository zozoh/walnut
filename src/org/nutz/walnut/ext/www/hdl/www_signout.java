package org.nutz.walnut.ext.www.hdl;

import org.nutz.json.Json;
import org.nutz.walnut.api.auth.WnAuthSession;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.www.impl.WnWebService;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.web.ajax.Ajax;
import org.nutz.web.ajax.AjaxReturn;

@JvmHdlParamArgs(value = "cqn", regex = "^(ajax)$")
public class www_signout implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // -------------------------------
        // 站点/票据
        String site = hc.params.val_check(0);
        String ticket = hc.params.val_check(1);

        // -------------------------------
        // 准备服务类
        WnObj oWWW = Wn.checkObj(sys, site);
        WnWebService webs = new WnWebService(sys, oWWW);

        // -------------------------------
        // 检查
        WnAuthSession se = webs.getAuthApi().logout(ticket, 0);
        if (null == se) {
            AjaxReturn re = Ajax.fail().setErrCode("e.www.logout.nosession");
            sys.out.println(Json.toJson(re));
            return;
        }

        // -------------------------------
        // 输出
        String json = se.formatJson(hc.jfmt, hc.params.is("ajax"));
        sys.out.println(json);
    }

}
