package com.site0.walnut.web2.controller;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.login.WnLoginApi;
import com.site0.walnut.login.WnLoginApiMaker;
import com.site0.walnut.login.WnLoginOptions;
import com.site0.walnut.login.session.WnSession;
import com.site0.walnut.login.site.WnLoginSite;
import com.site0.walnut.login.usr.WnUser;
import com.site0.walnut.mvc.WnMvcContext;
import com.site0.walnut.mvc.WnMvcView;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;
import com.site0.walnut.web.WnConfig;
import com.site0.walnut.web.util.WnAppModuleLoginMode;
import com.site0.walnut.web2.controller.app.WnOcmInfo;
import com.site0.walnut.web2.view.WnAddCookieView;

public class AppAuthLoginByDomainPasswdController extends AbstractAppAuthController {
    @Override
    public WnMvcView handle(WnMvcContext ctx) {
        NutMap params = params(ctx.request());
        String siteId = params.getString("site");
        String account = params.getString("name");
        String hostName = attr(ctx, "wn_www_host");
        WnIo io = io(ctx);
        WnLoginSite site = WnLoginSite.create(io, siteId, hostName);
        WnLoginApi sysAuth = auth(ctx);
        WnConfig conf = conf(ctx);
        String passwd = params.getString("passwd");
        String otpConfigName = params.getString("otp");
        String otpCode = params.getString("otp_val");
        WnOcmInfo ocm = get_ocm_info(ctx, null == site ? null : site.getDomain(), account);
        boolean ajax = bool(params, "ajax");
        cors(ctx);
        if (options(ctx))
            return null;
        Object reo = null;
        WnMvcView view = null;
        if (null == site) {
            if (ajax)
                view = WnMvcView.ajax("e.auth.login.domain_without_www", "e.auth.login.domain_without_www");
            else
                view = WnMvcView.redirect("/");
            if (log.isWarnEnabled())
                log.warnf("e.auth.login.domain_without_www: %s @ %s", siteId, hostName);
            reo = Er.create("e.auth.login.domain_without_www");
            return view;
        }
        String redirectPath = "/";
        WnAppModuleLoginMode mode = conf.getDomainLoginMode();
        try {
            boolean asDomainOwner = site.isDomainOwner(account);
            WnLoginApi auth;
            if (asDomainOwner) {
                auth = sysAuth;
            }
            else {
                WnLoginApiMaker loginMaker = WnLoginApiMaker.forHydrate();
                NutBean sessionVars = site.getSessionVarsBySiteHome();
                WnLoginOptions siteOptions = site.getOptions();
                auth = loginMaker.make(io, sessionVars, siteOptions);
            }
            if (log.isInfoEnabled())
                log.infof("Login as %s: %s", asDomainOwner ? "sys-usr" : "domain-user", account);
            WnUser user = auth.checkUser(account);
            switch (mode) {
            case OTP_ONLY:
                _assert_otp(ctx, user, ocm, otpConfigName, otpCode);
                break;
            case PASSWD_ONLY:
                __assert_passwd(user, passwd, asDomainOwner);
                break;
            case PASSWD_AND_OTP:
                _assert_otp(ctx, user, ocm, otpConfigName, otpCode);
                __assert_passwd(user, passwd, asDomainOwner);
                break;
            case PASSWD_OR_OTP:
                if (!Ws.isBlank(passwd))
                    __assert_passwd(user, passwd, asDomainOwner);
                else if (!Ws.isBlank(otpCode))
                    _assert_otp(ctx, user, ocm, otpConfigName, otpCode);
                else
                    throw Er.create(__passwd_err_code(asDomainOwner));
                break;
            }
            int se_du = auth.getSessionDuration();
            String seType = asDomainOwner ? Wn.SET_AUTH_PASS : Wn.SET_LOGIN_APP;
            WnSession se = auth.createSession(user, seType, se_du);
            if (!asDomainOwner)
                site.assertHomeAccessable(se);
            if (log.isInfoEnabled())
                log.infof("OK: create session: %s : %s", se.getTicket(), se.getMyName());
            String dftApp = asDomainOwner ? "wn-term" : "wn.manager";
            String appName = se.getEnv().getString("OPEN", dftApp);
            redirectPath = "/a/open/" + appName;
            reo = se.toBean(auth);
            _try_run_profile(ctx, se);
            if (ajax)
                view = new WnAddCookieView(conf, WnMvcView.ajax(reo), reo);
            else
                view = new WnAddCookieView(conf, WnMvcView.redirect(redirectPath), reo);
            return view;
        }
        catch (Exception e) {
            if (log.isWarnEnabled())
                log.warn(e.toString(), e);
            reo = e;
        }
        if (ajax)
            view = WnMvcView.ajax(Er.wrap((Throwable) reo).getKey(), Er.wrap((Throwable) reo).getKey());
        else
            view = WnMvcView.redirect(redirectPath);
        return view;
    }

    private void __assert_passwd(WnUser user, String passwd, boolean asDomainUser) {
        if (user.isMatchedRawPasswd(passwd))
            return;
        throw Er.create(__passwd_err_code(asDomainUser));
    }

    private String __passwd_err_code(boolean asDomainUser) {
        return asDomainUser ? "e.auth.login.Failed" : "e.auth.login.invalid.passwd";
    }
}
