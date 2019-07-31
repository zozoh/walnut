package org.nutz.walnut.ext.www.impl;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
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
    private long sessionDuration;

    private WnWebAuthService auth;

    private WnCaptchaService captcha;

    public WnWebService(WnSystem sys, WnObj oWWW, WnObj oDomain) {
        this.io = sys.io;
        String siteId = oWWW.id();

        this.oSessionHome = io.createIfExists(oDomain, "session/" + siteId, WnRace.DIR);
        this.oCaptchaHome = io.createIfNoExists(oDomain, "captcha/" + siteId, WnRace.DIR);

        NutMap site = oWWW.getAs("www_site", NutMap.class);

        if (site.has("accounts"))
            this.oAccountHome = Wn.checkObj(sys, site.getString("accounts"));

        if (site.has("roles"))
            this.oRoleHome = Wn.checkObj(sys, site.getString("roles"));

        if (site.has("orders"))
            this.oOrderHome = Wn.checkObj(sys, site.getString("orders"));

        this.sessionDuration = site.getLong("se_du", 86400);

        __init();
    }

    public WnWebService(WnIo io,
                        WnObj oSessionHome,
                        WnObj oCaptchaHome,
                        WnObj oAccountHome,
                        WnObj oRoleHome,
                        WnObj oOrderHome,
                        long sessionDuration) {
        this.io = io;
        this.oSessionHome = oSessionHome;
        this.oCaptchaHome = oCaptchaHome;
        this.oAccountHome = oAccountHome;
        this.oRoleHome = oRoleHome;
        this.oOrderHome = oOrderHome;
        this.sessionDuration = sessionDuration;
        __init();
    }

    private void __init() {
        // 权限服务
        this.auth = new WnWebAuthService(io,
                                         oAccountHome,
                                         oRoleHome,
                                         oSessionHome,
                                         sessionDuration);
        // 验证码服务
        this.captcha = new WnCaptchaService(io, oCaptchaHome);
    }

    ////////////////////////////////////////////////
    // 委托方法们
    ////////////////////////////////////////////////
    public WnWebSession checkMe(String ticket) {
        return auth.checkMe(ticket);
    }

    public WnWebSession loginByPasswd(String str, String passwd, boolean salted) {
        return auth.loginByPasswd(str, passwd, salted);
    }

    public WnObj saveCaptcha(WnCaptcha cap) {
        return captcha.saveCaptcha(cap);
    }

    public boolean removeCaptcha(String scene, String account, String code) {
        return captcha.removeCaptcha(scene, account, code);
    }

}
