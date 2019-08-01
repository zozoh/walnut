package org.nutz.walnut.ext.www.impl;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.weixin.WnIoWeixinApi;
import org.nutz.walnut.ext.www.bean.WnCaptcha;
import org.nutz.walnut.ext.www.bean.WnWebSession;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

public class WnWebService {

    private WnIo io;

    private WnObj oSessionHome;
    private WnObj oCaptchaHome;
    private WnObj oAccountHome;
    private WnObj oRoleHome;
    private WnObj oOrderHome;
    private WnObj oWxConf;
    private long sessionDuration;

    private WnCaptchaService captcha;

    private WnIoWeixinApi wxApi;

    private WnWebAuthService auth;

    public WnWebService(WnSystem sys, WnObj oWWW, WnObj oDomain) {
        this.io = sys.io;
        String siteId = oWWW.id();

        this.oSessionHome = io.createIfNoExists(oDomain, "session/" + siteId, WnRace.DIR);
        this.oCaptchaHome = io.createIfNoExists(oDomain, "captcha/" + siteId, WnRace.DIR);

        NutMap site = oWWW.getAs("www_site", NutMap.class);

        if (site.has("accounts"))
            this.oAccountHome = Wn.checkObj(sys, site.getString("accounts"));

        if (site.has("roles"))
            this.oRoleHome = Wn.checkObj(sys, site.getString("roles"));

        if (site.has("orders"))
            this.oOrderHome = Wn.checkObj(sys, site.getString("orders"));

        if (site.has("wxmp")) {
            String confPath = Wn.appendPath(site.getString("wxmp"), "wxconf");
            this.oWxConf = Wn.checkObj(sys, confPath);
        }

        this.sessionDuration = site.getLong("se_du", 86400);

        __init();
    }

    public WnWebService(WnIo io,
                        WnObj oSessionHome,
                        WnObj oCaptchaHome,
                        WnObj oAccountHome,
                        WnObj oRoleHome,
                        WnObj oOrderHome,
                        WnObj oWxConf,
                        long sessionDuration) {
        this.io = io;
        this.oSessionHome = oSessionHome;
        this.oCaptchaHome = oCaptchaHome;
        this.oAccountHome = oAccountHome;
        this.oRoleHome = oRoleHome;
        this.oOrderHome = oOrderHome;
        this.oWxConf = oWxConf;
        this.sessionDuration = sessionDuration;
        __init();
    }

    private void __init() {
        // 微信接口服务
        if (null != oWxConf) {
            this.wxApi = new WnIoWeixinApi(io, oWxConf);
        }

        // 验证码服务
        if (null != oCaptchaHome) {
            this.captcha = new WnCaptchaService(io, oCaptchaHome);
        }

        // 权限服务
        this.auth = new WnWebAuthService(io,
                                         captcha,
                                         wxApi,
                                         oAccountHome,
                                         oRoleHome,
                                         oSessionHome,
                                         sessionDuration);
    }

    public WnObj saveCaptcha(WnCaptcha cap) {
        return captcha.saveCaptcha(cap);
    }

    public boolean removeCaptcha(String scene, String account, String code) {
        return captcha.removeCaptcha(scene, account, code);
    }

    public String getDefaultRoleName() {
        return auth.getDefaultRoleName();
    }

    public WnWebSession getSession(String ticket) {
        return auth.getSession(ticket);
    }

    public WnWebSession checkSession(String ticket) {
        return auth.checkSession(ticket);
    }

    public WnWebSession loginByWxCode(String code) {
        return auth.loginByWxCode(code);
    }

    public WnWebSession bindAccount(String account, String vcode, String ticket) {
        return auth.bindAccount(account, vcode, ticket);
    }

    public WnWebSession loginByVcode(String account, String vcode) {
        return auth.loginByVcode(account, vcode);
    }

    public WnWebSession loginByPasswd(String account, String passwd) {
        return auth.loginByPasswd(account, passwd);
    }

    public WnWebSession logout(String ticket) {
        return auth.logout(ticket);
    }

}
