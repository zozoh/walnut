package com.site0.walnut.api.box;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.login.WnLoginApi;
import com.site0.walnut.login.WnLoginApiMaker;
import com.site0.walnut.login.WnSimpleLoginApi;
import com.site0.walnut.login.WnLoginOptions;
import com.site0.walnut.login.session.WnSession;
import com.site0.walnut.login.site.WnLoginSite;

public class WnBoxContext {

    public WnSession session;

    public WnIo io;

    public WnServiceHolder services;

    public NutMap attrs;

    private WnBoxContext() {}

    public WnBoxContext(WnServiceHolder services, NutMap attrs) {
        this.attrs = attrs;
        this.services = services;
    }

    public WnLoginApi auth() {
        if (null == session) {
            throw Er.create("e.box.auth.NoSession");
        }
        if (null == io) {
            throw Er.create("e.box.auth.NoIO");
        }
        // 域内站点
        String sitePath = session.getSite();
        if (null != sitePath) {
            WnLoginSite site = WnLoginSite.createByPath(io, sitePath);
            NutBean env = site.getSessionVarsBySiteHome();
            WnLoginOptions options = site.getOptions();
            // 2026-11-10 去 Nutz（web2）：hydrated 模式的系统会话存储不再取自 Nutz Ioc，
            // 显式沿用 services 系统登录 API 的会话存储（语义等价旧 sysSessionStore）
            WnLoginApi sysAuth = services.getLoginApi();
            WnLoginApi auth = WnLoginApiMaker.forHydrate()
                .make(io, env, options,
                      sysAuth instanceof WnSimpleLoginApi
                              ? ((WnSimpleLoginApi) sysAuth).getSessionStore()
                              : null);
            return auth;
        }

        // 采用系统权鉴接口
        return services.getLoginApi();
    }

    public WnBoxContext clone() {
        WnBoxContext bc = new WnBoxContext();
        bc.attrs = new NutMap();
        bc.services = this.services;
        bc.session = this.session.clone();
        bc.io = this.io;
        bc.attrs.putAll(this.attrs);
        return bc;
    }

}
