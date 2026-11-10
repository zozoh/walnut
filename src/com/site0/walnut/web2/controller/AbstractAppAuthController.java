package com.site0.walnut.web2.controller;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.login.WnLoginApi;
import com.site0.walnut.login.WnLoginApiMaker;
import com.site0.walnut.login.session.WnSession;
import com.site0.walnut.login.site.WnLoginSite;
import com.site0.walnut.login.usr.WnUser;
import com.site0.walnut.mvc.WnMvcContext;
import com.site0.walnut.security.captcha.WnCaptchaApi;
import com.site0.walnut.security.captcha.WnCaptchaBuilder;
import com.site0.walnut.security.captcha.impl.WnSysCaptchaBuilder;
import com.site0.walnut.security.otp.WnOTPService;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;
import com.site0.walnut.web2.controller.app.WnOcmInfo;

/**
 * App 认证入口公共逻辑：OCM/权鉴解析、captcha 构建、OTP 懒加载校验及登录后 profile。
 * 服务从请求上下文取用；OTP 实例仅在当前控制器请求内缓存。
 */
public abstract class AbstractAppAuthController extends AbstractAppController {

    protected static final Log log = Wlog.getAPP();

    private WnOTPService otps;

    protected WnOTPService _get_otps(WnMvcContext ctx, WnOcmInfo ocm, String otpConfigName) {
        if (null != this.otps)
            return this.otps;
        String otpName = Strings.sBlank(otpConfigName, "default");
        try {
            NutMap sessionVars = new NutMap();
            String confHome = ocm.getConfHomePath();
            sessionVars.put("HOME", confHome);
            this.otps = new WnOTPService(io(ctx), sessionVars, otpName);
        }
        catch (Exception e) {
            if (log.isWarnEnabled())
                log.warnf("Fail to create WnOTPService[%s] : %s", otpName, e.getMessage());
            this.otps = null;
            return null;
        }
        return this.otps;
    }

    protected String _check_otp(WnMvcContext ctx, WnUser user, WnOcmInfo ocm,
                               String otpConfigName, String otpCode) {
        if (Ws.isBlank(otpCode))
            return "e.auth.login.otp.required";
        String email = user.getEmail();
        if (Ws.isBlank(email))
            return "e.auth.login.otp.no_email";
        WnOTPService otps = _get_otps(ctx, ocm, otpConfigName);
        if (null == otps)
            return "e.auth.login.otp.unavailable";
        if (!otps.check(email, otpCode))
            return "e.auth.login.otp.invalid";
        return null;
    }

    protected void _assert_otp(WnMvcContext ctx, WnUser user, WnOcmInfo ocm,
                               String otpConfigName, String otpCode) {
        String otpErr = _check_otp(ctx, user, ocm, otpConfigName, otpCode);
        if (null != otpErr)
            throw Er.create(otpErr);
    }

    protected void _try_run_profile(WnMvcContext ctx, WnSession se) {
        services(ctx).getAppService().tryRunProfile(se);
    }

    protected WnCaptchaApi createCaptcha(WnMvcContext ctx, WnOcmInfo ocm, String captchaName) {
        String ccName = Strings.sBlank(captchaName, "default");
        NutMap vars = new NutMap();
        vars.put("HOME", ocm.getConfHomePath());
        WnCaptchaBuilder builder = new WnSysCaptchaBuilder(io(ctx), vars);
        return builder.build(ccName);
    }

    protected String captchaFailure(String errCode, Exception e) {
        NutMap re = new NutMap().setv("ok", false).setv("errCode", errCode);
        if (null != e)
            re.setv("msg", e.getMessage());
        return Json.toJson(re, JsonFormat.compact());
    }

    protected WnOcmInfo get_ocm_info(WnMvcContext ctx, String domain, String account) {
        WnOcmInfo info = new WnOcmInfo();
        info.setDomain(domain);
        info.setAccount(account);
        String confUserName = Ws.isBlank(domain) || domain.equals(account) ? "root" : domain;
        WnUser confUser = auth(ctx).checkUser(confUserName);
        info.setConfHomePath(confUser.getHomePath());
        try {
            info.setUser(get_auth(ctx, domain, account).checkUser(account));
        }
        catch (Exception e) {
            log.debug("App authentication account not resolved", e);
            info.setUser(null);
        }
        return info;
    }

    protected WnLoginApi get_auth(WnMvcContext ctx, String domain, String account) {
        if (Ws.isBlank(domain) || domain.equals(account))
            return auth(ctx);
        WnUser user = auth(ctx).checkUser(domain);
        NutBean vars = Wlang.map("HOME", user.getHomePath());
        String sitePath = Wn.normalizeFullPath("~/www/login", vars);
        WnLoginSite site = WnLoginSite.create(io(ctx), sitePath, null);
        if (null == site)
            throw Er.create("e.auth.login.domain_without_www", domain);
        return WnLoginApiMaker.forHydrate().make(io(ctx), site.getSessionVarsBySiteHome(), site.getOptions());
    }
}
