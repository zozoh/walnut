package com.site0.walnut.login;

import org.nutz.ioc.Ioc;
import org.nutz.lang.util.NutBean;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.ext.net.xapi.impl.WnXApi;
import com.site0.walnut.login.role.WnRoleStoreMaker;
import com.site0.walnut.login.session.WnSessionStore;
import com.site0.walnut.login.session.WnSessionStoreMaker;
import com.site0.walnut.login.session.WnSessionStoreProxy;
import com.site0.walnut.login.usr.WnUserStoreMaker;
import com.site0.walnut.util.Wn;
import com.site0.walnut.web.setup.WnSetup;

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
        return make(io, sessionVars, options, null);
    }

    /**
     * @param sysSessions
     *            系统会话存储（2026-11-10 去 Nutz/web2 新增）：hydrated 模式下优先采用；
     *            为 null 时回退旧的 Nutz Ioc 取 {@code sysSessionStore}（老环境语义不变）。
     */
    public WnLoginApi make(WnIo io,
                           NutBean sessionVars,
                           WnLoginOptions options,
                           WnSessionStore sysSessions) {

        WnSimpleLoginApi api = new WnSimpleLoginApi(io);
        
        // 混合模式，会话一定采用系统会话存储
        if (this.hydrated) {
            if (null != sysSessions) {
                api.sessions = sysSessions;
            }
            else {
                Ioc ioc = WnSetup.getIoc();
                // 2026-11-10 去 Nutz（web2）：Nutz Ioc 已不再装配（WnSetup.getIoc() 为 null），
                // 必须由调用方显式传入 sysSessions（cmd_login/AbstractAppAuthController 等已改）；
                // 旧 web 栈（RM-NUTZ-14 删除）在老环境仍有 ioc，不受影响
                if (null != ioc) {
                    api.sessions = ioc.get(WnSessionStoreProxy.class, "sysSessionStore");
                }
            }
        }
        // 采用指定的会话存储方式
        else {
            api.sessions = sessionStoreMaker.make(io, sessionVars, options.session);
        }

        // 用户接口
        api.users = userStoreMaker.make(io, sessionVars, options.user, options.domain);
        
        // 角色接口
        api.roles = roleStoreMaker.make(io, sessionVars, options.role);
        
        // 其他的属性
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
