package com.site0.walnut.web2.service.register;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.ext.net.xapi.impl.WnXApi;
import com.site0.walnut.login.WnSimpleLoginApi;
import com.site0.walnut.login.role.WnRoleStoreProxy;
import com.site0.walnut.login.role.WnStdRoleStore;
import com.site0.walnut.login.session.WnSessionStoreProxy;
import com.site0.walnut.login.session.WnStdSessionStore;
import com.site0.walnut.login.usr.WnStdUserStore;
import com.site0.walnut.login.usr.WnUserStoreProxy;
import com.site0.walnut.web.WnConfig;
import com.site0.walnut.web2.service.WnServiceFactory;

/**
 * 装配账户/会话/角色三层 store 与 {@code sysLoginApi}（旧 {@code service.js} 片段）。
 * <p>
 * 必须在关键目录（/sys/usr、/sys/role、/var/session）建好之后调用。
 * <p>
 * 仅装配期使用：无状态、无单例，不是运行期服务入口。
 */
public final class WnLoginRegistrar {

    private WnLoginRegistrar() {}

    public static void register(WnServiceFactory factory) {
        WnConfig conf = factory.getConfig();
        WnIo io = factory.fetch("io", WnIo.class);

        // 账户 / 会话 / 角色三层 store
        WnStdUserStore rawUserStore = new WnStdUserStore(io, conf);
        factory.register("rawUserStore", rawUserStore);

        WnUserStoreProxy sysUserStore = new WnUserStoreProxy(io, rawUserStore);
        factory.register("sysUserStore", sysUserStore);

        WnStdSessionStore rawSessionStore = new WnStdSessionStore(io, conf);
        factory.register("rawSessionStore", rawSessionStore);

        WnSessionStoreProxy sysSessionStore = new WnSessionStoreProxy(io, rawSessionStore);
        factory.register("sysSessionStore", sysSessionStore);

        WnStdRoleStore rawRoleStore = new WnStdRoleStore(io);
        factory.register("rawRoleStore", rawRoleStore);

        WnRoleStoreProxy sysRoleStore = new WnRoleStoreProxy(io, rawRoleStore);
        factory.register("sysRoleStore", sysRoleStore);

        WnXApi sysXApi = new WnXApi(io);
        factory.register("sysXApi", sysXApi);

        WnSimpleLoginApi sysLoginApi = new WnSimpleLoginApi(io);
        sysLoginApi.setUserStore(sysUserStore);
        sysLoginApi.setSessionStore(sysSessionStore);
        sysLoginApi.setRoleStore(sysRoleStore);
        sysLoginApi.setXapi(sysXApi);
        sysLoginApi.setDomain("root");
        sysLoginApi.setSessionDuration(conf.getInt("se-sys-du", 3600));
        sysLoginApi.setSessionShortDu(conf.getInt("se-tmp-du", 10));
        sysLoginApi.setWechatMpOpenIdKey(conf.get("wx-mp-open-id-key", "wxmp_openid"));
        sysLoginApi.setWechatGhOpenIdKey(conf.get("wx-gh-open-id-key", "wxgh_openid"));
        factory.register("sysLoginApi", sysLoginApi);
    }

}
