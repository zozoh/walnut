package org.nutz.walnut.ext.www.hdl;

import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.auth.WnAuths;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.www.cmd_www;
import org.nutz.walnut.ext.www.impl.WnWebService;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.web.WebException;
import org.nutz.web.ajax.Ajax;
import org.nutz.web.ajax.AjaxReturn;

@JvmHdlParamArgs(value = "cqn", regex = "^(ajax|init)$")
public class www_passwd implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        try {
            // -------------------------------
            // 站点/账户/密码/票据
            String site = hc.params.val_check(0);
            String ticket = hc.params.get("ticket");
            String unm = hc.params.get("u");
            // -------------------------------
            // 准备服务类
            WnObj oWWW = cmd_www.checkSite(sys, site);
            WnWebService webs = new WnWebService(sys, oWWW);
            // -------------------------------
            // 确定用户
            WnAccount u = cmd_www.checkTargetUser(sys, webs, unm, ticket);
            // -------------------------------
            // 准备密码的修改
            String passwd;
            // 校验密码
            if (hc.params.has("check")) {
                String json = Cmds.checkParamOrPipe(sys, hc.params, "check", true);
                NutMap form = Lang.map(json);
                // 采用旧密码校验
                String oldpwd = form.getString("oldpwd");
                String newpwd = form.getString("newpwd");

                // 新密码无效
                if (Strings.isBlank(newpwd) || newpwd.length() < 6) {
                    throw Er.create("e.cmd.www_passwd.InvalidNewPasswd");
                }

                // 采用旧密码校验
                if (!Strings.isBlank(oldpwd)) {
                    if (u.hasSaltedPasswd() && !u.isMatchedRawPasswd(oldpwd)) {
                        throw Er.create("e.cmd.www_passwd.CheckFailed");
                    }
                }
                // 采用验证码验证
                else {
                    String account = form.getString("account");
                    String scene = form.getString("scene", "resetpasswd");
                    String code = form.getString("vcode");

                    // 没有验证码
                    if (Strings.isBlank(code)) {
                        throw Er.create("e.cmd.www_passwd.CheckBlankCode");
                    }

                    // 没有账户
                    if (Strings.isBlank(account)) {
                        throw Er.create("e.cmd.www_passwd.CheckBlankAccount");
                    }

                    // 那么看一看，是邮箱还是电话, 不是邮箱也不是电话，抛错啊
                    if (!u.isMyEmail(account) && !u.isMyPhone(account)) {
                        throw Er.create("e.cmd.www_passwd.CheckWeirdAccount");
                    }

                    // 校验一下验证码
                    if (!webs.getCaptchaApi().removeCaptcha(scene, account, code)) {
                        throw Er.create("e.cmd.www_passwd.CheckCodeFail");
                    }
                }

                // 嗯，设置新密码
                passwd = newpwd;
            }
            // 直接修改密码
            else {
                passwd = hc.params.val_check(1);
            }
            // -------------------------------
            // 新密码格式要对
            if (Strings.isBlank(passwd)) {
                throw Er.create("e.cmd.www_passwd.Blank");
            }
            if (passwd.length() < 4) {
                throw Er.create("e.cmd.www_passwd.TooShort");
            }
            // -------------------------------
            // 设置初始化密码
            if (hc.params.is("init")) {
                if (!u.hasSaltedPasswd()) {
                    u.setRawPasswd(passwd);
                    webs.getAuthApi().saveAccount(u, WnAuths.ABMM.PASSWD);
                }
            }
            // 设置并保存新密码
            else {
                u.setRawPasswd(passwd);
                webs.getAuthApi().saveAccount(u, WnAuths.ABMM.PASSWD);
            }
            // -------------------------------
            // 输出
            if (hc.params.is("ajax")) {
                AjaxReturn re = Ajax.ok().setData(u.toBeanForClient());
                String json = Json.toJson(re, hc.jfmt);
                sys.out.println(json);
            }
        }
        // 捕捉错误输出
        catch (Exception e) {
            WebException err = Er.wrap(e);
            if (hc.params.is("ajax")) {
                AjaxReturn re = Ajax.fail();
                re.setErrCode(err.getKey());
                re.setMsg(err.getReasonString());
                re.setData(err.getReason());
                String json = Json.toJson(re, hc.jfmt);
                sys.out.println(json);
            }
            // 直接抛咯
            else {
                throw err;
            }
        }
    }

}
