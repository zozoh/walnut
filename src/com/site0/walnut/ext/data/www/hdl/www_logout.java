package com.site0.walnut.ext.data.www.hdl;

import org.nutz.json.Json;
import org.nutz.lang.Strings;
import com.site0.walnut.api.auth.WnAuthSession;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.data.www.cmd_www;
import com.site0.walnut.ext.data.www.impl.WnWebService;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;
import org.nutz.web.WebException;
import org.nutz.web.ajax.Ajax;
import org.nutz.web.ajax.AjaxReturn;

@JvmHdlParamArgs(value = "cqn", regex = "^(ajax)$")
public class www_logout implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // -------------------------------
        // 站点/票据
        String site = hc.params.val(0);
        String ticket = hc.params.val(1);
        boolean ajax = hc.params.is("ajax");

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
                se = webs.getAuthApi().logout(ticket, 0);

                // 输出: 子会话
                if (null != se) {
                    String json = se.formatJson(hc.jfmt, ajax);
                    sys.out.println(json);
                    return;
                }
                // 顶级会话退出的话，就没啥咯
                re = Ajax.ok();
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
