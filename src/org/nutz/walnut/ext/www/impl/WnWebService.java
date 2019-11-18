package org.nutz.walnut.ext.www.impl;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.auth.WnCaptcha;
import org.nutz.walnut.api.auth.WnCaptchaService;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.weixin.WnIoWeixinApi;
import org.nutz.walnut.ext.www.bean.WnOrder;
import org.nutz.walnut.ext.www.bean.WnWebSession;
import org.nutz.walnut.impl.auth.WnCaptchaServiceImpl;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

public class WnWebService {

    private WnIo io;

    private WnObj oSessionHome;
    private WnObj oCaptchaHome;
    private WnObj oAccountHome;
    private WnObj oRoleHome;
    private WnObj oOrderHome;
    private WnObj oProductHome;
    private WnObj oCouponHome;
    private WnObj oWxConf;
    private NutMap sellers;
    private long sessionDuration;

    private WnCaptchaService captcha;

    private WnOrderService order;

    private WnIoWeixinApi wxApi;

    private WnWebAuthService auth;

    public WnWebService(WnSystem sys, WnObj oWWW, WnObj oDomain) {
        this.io = sys.io;
        String siteId = oWWW.id();

        this.oSessionHome = io.createIfNoExists(oDomain, "session/" + siteId, WnRace.DIR);
        this.oCaptchaHome = io.createIfNoExists(oDomain, "captcha/" + siteId, WnRace.DIR);

        NutBean site = oWWW;

        if (site.has("accounts"))
            this.oAccountHome = Wn.checkObj(sys, site.getString("accounts"));

        if (site.has("roles"))
            this.oRoleHome = Wn.checkObj(sys, site.getString("roles"));

        if (site.has("orders"))
            this.oOrderHome = Wn.checkObj(sys, site.getString("orders"));

        if (site.has("products"))
            this.oProductHome = Wn.checkObj(sys, site.getString("products"));

        if (site.has("coupons"))
            this.oCouponHome = Wn.checkObj(sys, site.getString("coupons"));

        if (site.has("weixin")) {
            String confPath = Wn.appendPath("~/.weixin", site.getString("weixin"), "wxconf");
            this.oWxConf = Wn.checkObj(sys, confPath);
        }

        if (site.has("sellers")) {
            this.sellers = site.getAs("sellers", NutMap.class);
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
                        WnObj oProductHome,
                        WnObj oCouponHome,
                        WnObj oWxConf,
                        long sessionDuration) {
        this.io = io;
        this.oSessionHome = oSessionHome;
        this.oCaptchaHome = oCaptchaHome;
        this.oAccountHome = oAccountHome;
        this.oRoleHome = oRoleHome;
        this.oOrderHome = oOrderHome;
        this.oProductHome = oProductHome;
        this.oCouponHome = oCouponHome;
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
            this.captcha = new WnCaptchaServiceImpl(io, oCaptchaHome);
        }

        // 订单服务
        if (null != oOrderHome && null != oProductHome) {
            order = new WnOrderService(io, oOrderHome, oProductHome, oCouponHome, sellers);
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

    public WnObj getSessionHome() {
        return oSessionHome;
    }

    public WnObj getCaptchaHome() {
        return oCaptchaHome;
    }

    public WnObj getAccountHome() {
        return oAccountHome;
    }

    public WnObj getRoleHome() {
        return oRoleHome;
    }

    public WnObj getOrderHome() {
        return oOrderHome;
    }

    public WnObj getProductHome() {
        return oProductHome;
    }

    public WnObj getCouponHome() {
        return oCouponHome;
    }

    public long getSessionDuration() {
        return sessionDuration;
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

    public WnWebSession removeSession(String ticket) {
        return auth.removeSession(ticket);
    }

    public WnWebSession loginByWxCode(String code) {
        return auth.loginByWxCode(code);
    }

    public WnWebSession bindAccount(String account, String scene, String vcode, String ticket) {
        return auth.bindAccount(account, scene, vcode, ticket);
    }

    public WnWebSession loginByVcode(String account, String scene, String vcode) {
        return auth.loginByVcode(account, scene, vcode);
    }

    public WnWebSession loginByPasswd(String account, String passwd) {
        return auth.loginByPasswd(account, passwd);
    }

    public WnWebSession logout(String ticket) {
        return auth.logout(ticket);
    }

    public WnOrder createOrder(WnOrder or) {
        return order.createOrder(or);
    }

    public WnOrder checkOrder(String id) {
        return order.checkOrder(id);
    }

    public WnOrder getOrder(String id) {
        return order.getOrder(id);
    }

}
