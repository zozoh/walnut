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
            WnAccount me = sys.getMe();
            WnAccount u;
            // -u 模式，则当前操作会话必须为站点管理员或者 root/op组成员
            if (!Strings.isBlank(unm)) {
                // 检查权限
                if (!sys.auth.isAdminOfGroup(me, oWWW.group())) {
                    if (!sys.auth.isMemberOfGroup(me, "root", "op")) {
                        throw Er.create("e.cmd.www_passwd.nopvg");
                    }
                }
                // 通过
                u = webs.getAuthApi().checkAccount(unm);
            }
            // 否则就用当前会话
            else if (!Strings.isBlank(ticket)) {
                u = webs.getAuthApi().checkSession(ticket).getMe();
            }
            // unm/ticket 必须得有一个啊
            else {
                throw Er.create("e.cmd.www_passwd.LackTarget");
            }
            // -------------------------------
            // 准备密码的修改
            String passwd;
            // 校验密码
            if (hc.params.has("check")) {
                String json = Cmds.checkParamOrPipe(sys, hc.params, "check", true);
                NutMap form = Lang.map(json);
                String oldpwd = form.getString("oldpwd");
                String newpwd = form.getString("newpwd");
                if (u.hasSaltedPasswd() && !u.isMatchedRawPasswd(oldpwd)) {
                    throw Er.create("e.cmd.www_passwd.CheckFailed");
                }
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
