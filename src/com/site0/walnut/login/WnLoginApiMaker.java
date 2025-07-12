package com.site0.walnut.login;

import org.nutz.lang.util.NutBean;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.ext.net.xapi.impl.WnXApi;
import com.site0.walnut.login.role.WnRoleStoreMaker;
import com.site0.walnut.login.session.WnSessionStoreMaker;
import com.site0.walnut.login.usr.WnUserStoreMaker;

public class WnLoginApiMaker {

    private static final WnLoginApiMaker for_domain = new WnLoginApiMaker(UserRace.DOMAIN);
    private static final WnLoginApiMaker for_sys = new WnLoginApiMaker(UserRace.SYS);

    public static WnLoginApiMaker forDomain() {
        return for_domain;
    }

    public static WnLoginApiMaker forSys() {
        return for_sys;
    }

    private WnSessionStoreMaker sessionStoreMaker;

    private WnUserStoreMaker userStoreMaker;

    private WnRoleStoreMaker roleStoreMaker;

    public WnLoginApiMaker(UserRace userRace) {
        userStoreMaker = new WnUserStoreMaker(userRace);
        sessionStoreMaker = new WnSessionStoreMaker();
        roleStoreMaker = new WnRoleStoreMaker();
    }

    public WnLoginApi make(WnIo io, NutBean sessionVars, WnLoginOptions options) {

        WnLoginApi api = new WnLoginApi(io);
        api.users = userStoreMaker.make(io, sessionVars, options.user);
        api.sessions = sessionStoreMaker.make(io, sessionVars, options.session);
        api.roles = roleStoreMaker.make(io, sessionVars, options.role);
        api.domain = options.domain;
        api.sessionDuration = options.sessionDuration;
        api.wechatGhOpenIdKey = options.wechatGhOpenIdKey;
        api.wechatMpOpenIdKey = options.wechatMpOpenIdKey;

        // 获取 Xapi
        api.xapi = new WnXApi(io, sessionVars);

        return api;

    }

}
