package com.site0.walnut.ext.data.www.hdl;

import org.nutz.json.Json;
import org.nutz.lang.Strings;
import com.site0.walnut.api.auth.WnAccount;
import com.site0.walnut.api.auth.WnAuthService;
import com.site0.walnut.api.auth.WnAuthSession;
import com.site0.walnut.api.auth.WnAuths;
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

@JvmHdlParamArgs(value = "cqn", regex = "^(create|ajax|subscribe)$")
public class www_auth implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // -------------------------------
        // 站点/账户/密码/票据
        WnObj oWWW = cmd_www.checkSite(sys, hc);
        String account = hc.params.val_check(1);
        String passwd = hc.params.get("p");
        String vcode = hc.params.get("v");
        String ticket = hc.params.get("ticket");
        String wxCodeType = hc.params.get("wxcode");
        // -------------------------------
        // 准备服务类
        WnWebService webs = new WnWebService(sys, oWWW);
        WnAuthSession se = null;

        try {
            WnAuthService auth = webs.getAuthApi();
            // -------------------------------
            // 微信票据代码登录
            if (!Strings.isBlank(wxCodeType)) {
                boolean subscribe = hc.params.is("subscribe", false);
                se = auth.loginByWxCode(account, wxCodeType, subscribe);
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
            // -------------------------------
            // 指定直接创建一个 Session
            else if (hc.params.is("create")) {
                WnAccount u = auth.checkAccount(account);
                se = auth.createSession(u, true);
            }

            // 如果指定了密码，且当前账户没有设定密码，作为初始化密码设置进去
            if (null != se) {
                WnAccount u = se.getMe();
                if (!Strings.isBlank(passwd) && !u.hasSaltedPasswd()) {
                    u.setRawPasswd(passwd);
                    auth.saveAccount(u, WnAuths.ABMM.PASSWD);
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
