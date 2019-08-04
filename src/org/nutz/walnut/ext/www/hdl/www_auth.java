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

@JvmHdlParamArgs(value = "cqn", regex = "^(ajax|wxcode)$")
public class www_auth implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // -------------------------------
        // 站点/账户/密码/票据
        String site = hc.params.val_check(0);
        String account = hc.params.val_check(1);
        String passwd = hc.params.get("p");
        String vcode = hc.params.get("v");
        String ticket = hc.params.get("ticket");

        // -------------------------------
        // 准备服务类
        WnObj oWWW = Wn.checkObj(sys, site);
        WnObj oDomain = Wn.checkObj(sys, "~/.domain");
        WnWebService webs = new WnWebService(sys, oWWW, oDomain);
        WnWebSession se = null;

        try {
            // -------------------------------
            // 微信票据代码登录
            if (hc.params.is("wxcode")) {
                se = webs.loginByWxCode(account);
            }
            // -------------------------------
            // 密码登录
            else if (!Strings.isBlank(passwd)) {
                se = webs.loginByPasswd(account, passwd);
            }
            // -------------------------------
            // 验证码 /绑定
            else if (!Strings.isBlank(vcode)) {
                // 绑定手机
                if (!Strings.isBlank(ticket)) {
                    se = webs.bindAccount(account, vcode, ticket);
                }
                // 验证码登录
                else {
                    se = webs.loginByVcode(account, vcode);
                }
            }
        }
        // 有点错
        catch (Exception e) {
            WebException we = Er.wrap(e);
            AjaxReturn re = Ajax.fail();
            re.setErrCode(we.getKey());
            re.setData(we.getReason());
            String json = Json.toJson(re, hc.jfmt);
            sys.out.println(json);
            return;
        }

        // -------------------------------
        // 会话创建失败
        if (null == se) {
            AjaxReturn re = Ajax.fail().setErrCode("e.www.api.auth.fail_login");
            String json = Json.toJson(re, hc.jfmt);
            sys.out.println(json);
            return;
        }
        // -------------------------------
        // 输出
        else {
            String json = se.formatJson(hc.jfmt, hc.params.is("ajax"));
            sys.out.println(json);
        }
    }

}
