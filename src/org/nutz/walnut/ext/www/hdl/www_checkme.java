package org.nutz.walnut.ext.www.hdl;

import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.www.bean.WnWebSession;
import org.nutz.walnut.ext.www.impl.WnWebService;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.web.WebException;
import org.nutz.web.ajax.Ajax;
import org.nutz.web.ajax.AjaxReturn;

@JvmHdlParamArgs(value = "cqn", regex = "^(ajax)$")
public class www_checkme implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // -------------------------------
        // 站点/票据
        String site = hc.params.val(0);
        String ticket = hc.params.val(1);

        // -------------------------------
        // 检查会话
        WnWebSession se = null;

        try {
            if (!Strings.isBlank(site) && !Strings.isBlank(ticket)) {
                // 准备服务类
                WnObj oWWW = Wn.checkObj(sys, site);
                WnObj oDomain = Wn.checkObj(sys, "~/.domain");
                WnWebService webs = new WnWebService(sys, oWWW, oDomain);

                // 检查
                se = webs.checkSession(ticket);

                // 输出
                String json = se.formatJson(hc.jfmt, hc.params.is("ajax"));
                sys.out.println(json);
            }
        }
        // -------------------------------
        // 错误
        catch (Throwable e) {
            WebException we = Er.wrap(e);
            AjaxReturn re = Ajax.fail().setErrCode("e.www.api.auth.nologin");
            re.setData(we.toString());
            String json = Json.toJson(re, hc.jfmt);
            sys.out.println(json);
            return;
        }
    }

}
