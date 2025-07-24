package com.site0.walnut.login;

import org.nutz.ioc.Ioc;
import org.nutz.lang.util.NutBean;
import org.nutz.mvc.Mvcs;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.ext.net.xapi.impl.WnXApi;
import com.site0.walnut.login.role.WnRoleStoreMaker;
import com.site0.walnut.login.session.WnSessionStoreMaker;
import com.site0.walnut.login.session.WnSessionStoreProxy;
import com.site0.walnut.login.usr.WnUserStoreMaker;
import com.site0.walnut.util.Wn;

public class WnLoginApiMaker {

    private static final WnLoginApiMaker for_domain = new WnLoginApiMaker(UserRace.DOMAIN);
    private static final WnLoginApiMaker for_sys = new WnLoginApiMaker(UserRace.SYS);
    private static final WnLoginApiMaker for_hydrate = new WnLoginApiMaker(UserRace.SYS, true);

    public static WnLoginApiMaker forDomain() {
        return for_domain;
    }

    public static WnLoginApiMaker forSys() {
        return for_sys;
    }

    public static WnLoginApiMaker forHydrate() {
        return for_hydrate;
    }

    /**
     * <h3>混合模式</h3> 为系统多账号登录特意建立一个混合实现
     * 
     * 它维护 session 是用系统的会话存储 而用户和角色则采用给定的权鉴接口。 <br>
     * 因此创建实例是，会无视会话存储选项，<br>
     * 直接从 Ioc 容器获取 "sysSessionStore" 作为 Session 存储接口
     * 
     * 同时，它会标志 sessionStore 让其创建会话对象时，会增加一个 site 的属性<br>
     * 这样，"sysLoginApi"获取的 Session 也都会自动采用对应 site 的用户/角色存储
     * 
     * @author zozoh(zozohtnt@gmail.com)
     */
    private boolean hydrated;

    private WnSessionStoreMaker sessionStoreMaker;

    private WnUserStoreMaker userStoreMaker;

    private WnRoleStoreMaker roleStoreMaker;

    public WnLoginApiMaker(UserRace userRace) {
        this(userRace, false);
    }

    public WnLoginApiMaker(UserRace userRace, boolean hydrated) {
        this.hydrated = hydrated;
        this.userStoreMaker = new WnUserStoreMaker(userRace);
        this.sessionStoreMaker = new WnSessionStoreMaker();
        this.roleStoreMaker = new WnRoleStoreMaker();
    }

    public WnLoginApi make(WnIo io, NutBean sessionVars, WnLoginOptions options) {

        WnSimpleLoginApi api = new WnSimpleLoginApi(io);
        // 混合模式，会话一定采用系统会话存储
        if (this.hydrated) {
            Ioc ioc = Mvcs.getIoc();
            api.sessions = ioc.get(WnSessionStoreProxy.class, "sysSessionStore");
        }
        // 采用指定的会话存储方式
        else {
            api.sessions = sessionStoreMaker.make(io, sessionVars, options.session);
        }

        api.users = userStoreMaker.make(io, sessionVars, options.user, options.domain);

        api.roles = roleStoreMaker.make(io, sessionVars, options.role);
        api.domain = options.domain;
        api.site = Wn.normalizeFullPath(options.site, sessionVars);
        api.sessionDuration = options.sessionDuration;
        api.sessionShortDu = options.sessionShortDu;
        api.wechatGhOpenIdKey = options.wechatGhOpenIdKey;
        api.wechatMpOpenIdKey = options.wechatMpOpenIdKey;

        // 获取 Xapi
        api.xapi = new WnXApi(io, sessionVars);

        return api;

    }

}
