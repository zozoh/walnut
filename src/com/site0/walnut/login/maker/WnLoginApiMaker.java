package com.site0.walnut.login.maker;

import org.nutz.lang.util.NutBean;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.ext.net.xapi.impl.WnXApi;
import com.site0.walnut.login.UserRace;
import com.site0.walnut.login.WnLoginApi;
import com.site0.walnut.login.WnLoginOptions;
import com.site0.walnut.login.WnLoginSetup;

public class WnLoginApiMaker {

    private static final WnLoginApiMaker for_domain = new WnLoginApiMaker();
    // private static final WnLoginApiMaker for_sys = new
    // WnLoginApiMaker(UserRace.SYS);

    public static WnLoginApiMaker forDomain() {
        return for_domain;
    }

    private WnSessionStoreMaker sessionStoreMaker;

    private WnUserStoreMaker userStoreMaker;

    public WnLoginApiMaker() {
        userStoreMaker = new WnUserStoreMaker(UserRace.DOMAIN);
        sessionStoreMaker = new WnSessionStoreMaker();
    }

    public WnLoginApi make(WnIo io, NutBean sessionVars, WnLoginOptions options) {

        WnLoginSetup setup = new WnLoginSetup();
        setup.users = userStoreMaker.make(io, sessionVars, options.user);
        setup.sessions = sessionStoreMaker.make(io, sessionVars, options.session);
        setup.domain = options.domain;
        setup.sessionDuration = options.sessionDuration;
        setup.wechatGhOpenIdKey = options.wechatGhOpenIdKey;
        setup.wechatMpOpenIdKey = options.wechatMpOpenIdKey;

        // 获取 Xapi
        setup.xapi = new WnXApi(io, sessionVars);

        return new WnLoginApi(setup);

    }

}
