package org.nutz.walnut.ext.www.hdl;

import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.nutz.walnut.api.auth.WnAuthService;
import org.nutz.walnut.api.auth.WnAuthSession;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
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
        String wxCodeType = hc.params.get("wxcode");

        // -------------------------------
        // 准备服务类
        WnObj oWWW = Wn.checkObj(sys, site);
        WnWebService webs = new WnWebService(sys, oWWW);
        WnAuthSession se = null;

        try {
            WnAuthService auth = webs.getAuthApi();
            // -------------------------------
            // 微信票据代码登录
            if (!Strings.isBlank(wxCodeType)) {
                se = auth.loginByWxCode(account, wxCodeType);
            }
            // -------------------------------
            // 密码登录
            else if (!Strings.isBlank(passwd)) {
                se = auth.loginByPasswd(account, passwd);
            }
            // -------------------------------
            // 验证码 /绑定
            else if (!Strings.isBlank(vcode)) {
                String scene = hc.params.getString("scene", "auth");
                // 绑定手机
                if (!Strings.isBlank(ticket)) {
                    se = auth.bindAccount(account, scene, vcode, ticket);
                }
                // 验证码登录
                else {
                    se = auth.loginByVcode(account, scene, vcode);
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
