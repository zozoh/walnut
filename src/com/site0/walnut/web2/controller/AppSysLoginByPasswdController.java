package com.site0.walnut.web2.controller;

import com.site0.walnut.util.Ws;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.login.WnLoginApi;
import com.site0.walnut.login.session.WnSession;
import com.site0.walnut.login.usr.WnUser;
import com.site0.walnut.mvc.WnMvcContext;
import com.site0.walnut.mvc.WnMvcView;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.WnConfig;
import com.site0.walnut.util.WnAppModuleLoginMode;
import com.site0.walnut.web2.controller.app.WnOcmInfo;
import com.site0.walnut.web2.view.WnAddCookieView;
import redis.clients.jedis.exceptions.JedisException;

public class AppSysLoginByPasswdController extends AbstractAppAuthController {
    @Override
    public WnMvcView handle(WnMvcContext ctx) {
        NutMap params = params(ctx.request());
        String name = params.getString("name");
        WnLoginApi sysAuth = auth(ctx);
        WnConfig conf = conf(ctx);
        String passwd = params.getString("passwd");
        String otpConfigName = params.getString("otp");
        String otpCode = params.getString("otp_val");
        WnOcmInfo ocm = get_ocm_info(ctx, null, name);
        boolean ajax = bool(params, "ajax");
        String referer = ctx.request().getHeader("Referer");
        WnAppModuleLoginMode mode = conf.getSysLoginMode();
        try {
            WnUser user = sysAuth.checkUser(name);
            switch (mode) {
            case OTP_ONLY:
                _assert_otp(ctx, user, ocm, otpConfigName, otpCode);
                break;
            case PASSWD_ONLY:
                __assert_passwd(user, passwd);
                break;
            case PASSWD_AND_OTP:
                _assert_otp(ctx, user, ocm, otpConfigName, otpCode);
                __assert_passwd(user, passwd);
                break;
            case PASSWD_OR_OTP:
                if (!Ws.isBlank(passwd))
                    __assert_passwd(user, passwd);
                if (!Ws.isBlank(otpCode))
                    _assert_otp(ctx, user, ocm, otpConfigName, otpCode);
                if (Ws.isBlank(passwd) && Ws.isBlank(otpCode))
                    throw Er.create("e.auth.login.Failed");
                break;
            }
            int se_du = sysAuth.getSessionDuration();
            WnSession se = sysAuth.createSession(user, Wn.SET_AUTH_PASS, se_du);
            _try_run_profile(ctx, se);
            if (ajax) {
                Object reo = se.toBean(sysAuth);
                return new WnAddCookieView(conf, WnMvcView.ajax(reo), reo);
            }
            return __get_session_default_view(ctx, se);
        }
        catch (RuntimeException e) {
            if (Er.wrap(e) != e)
                throw e;
            Throwable eCause = e.getCause();
            if (null != eCause && (eCause instanceof JedisException)) {
                if (log.isWarnEnabled())
                    log.warn("Jedis error", eCause);
            }
            Object reo = new NutMap().setv("ok", false)
                .setv("errCode", Er.wrap(e).getKey())
                .setv("data", Er.wrap(e).getReason());
            if (ajax)
                return WnMvcView.json(reo);
            String redirectTo = Ws.sBlank(referer, "/");
            return WnMvcView.redirect(redirectTo);
        }
    }

    private void __assert_passwd(WnUser user, String passwd) {
        if (user.isMatchedRawPasswd(passwd))
            return;
        throw Er.create("e.auth.login.Failed");
    }

    private WnMvcView __get_session_default_view(WnMvcContext ctx, WnSession se) {
        String appName = se.getEnv().getString("OPEN", "wn.console");
        String url = "/a/open/" + appName;
        Object reData = se.toBean(auth(ctx));
        return new WnAddCookieView(conf(ctx), WnMvcView.redirect(url), reData);
    }
}
