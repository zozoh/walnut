package com.site0.walnut.ext.data.www.hdl;

import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.auth.WnAccount;
import com.site0.walnut.api.auth.WnAuthSession;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.data.www.cmd_www;
import com.site0.walnut.ext.data.www.impl.WnWebService;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import org.nutz.web.WebException;
import org.nutz.web.ajax.Ajax;
import org.nutz.web.ajax.AjaxReturn;

@JvmHdlParamArgs(value = "cqn", regex = "^(ajax)$")
public class www_checkme implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // -------------------------------
        // 站点/票据
        String site = hc.params.val_check(0);
        String ticket = hc.params.val(1);

        // -------------------------------
        // 检查会话
        WnAuthSession se = null;
        AjaxReturn re = Ajax.fail().setErrCode("e.www.api.auth.nologin");
        try {
            if (!Strings.isBlank(site) && !Strings.isBlank(ticket)) {
                // 准备服务类
                WnObj oWWW = cmd_www.checkSite(sys, site);
                WnWebService webs = new WnWebService(sys, oWWW);

                // 检查
                se = webs.getAuthApi().checkSession(ticket);

                // 修改账户信息
                if (hc.params.has("u")) {
                    String json = Cmds.getParamOrPipe(sys, hc.params, "u", true);
                    if (!Strings.isBlank(json)) {
                        NutMap meta = Lang.map(json);
                        WnAccount u = se.getMe();
                        u = webs.getAuthApi().saveAccount(u, meta);
                        se.setMe(u);
                    }
                }

                // 输出
                NutMap seMap = se.toMapForClient();
                cmd_www.outputJsonOrAjax(sys, seMap, hc);
                return;
            }
        }
        // -------------------------------
        // 错误
        catch (Throwable e) {
            WebException we = Er.wrap(e);
            re.setData(we.toString());
        }

        // 错误输出
        String json = Json.toJson(re, hc.jfmt);
        sys.out.println(json);
    }

}
