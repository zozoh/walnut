package com.site0.walnut.web2.router;

import com.site0.walnut.mvc.WnMvcFilter;
import com.site0.walnut.mvc.WnMvcHttpMethod;
import com.site0.walnut.mvc.WnMvcMapping;
import com.site0.walnut.web2.controller.AppLoginPageController;
import com.site0.walnut.web2.controller.AppOpenController;
import com.site0.walnut.web2.controller.AppLoadController;
import com.site0.walnut.web2.controller.AppBeaconRunController;
import com.site0.walnut.web2.controller.AppRunController;
import com.site0.walnut.web2.controller.AppSysLoginByPasswdController;
import com.site0.walnut.web2.controller.AppSysLogoutController;
import com.site0.walnut.web2.controller.AppAuthLoginByDomainPasswdController;
import com.site0.walnut.web2.controller.AppCaptchaGetController;
import com.site0.walnut.web2.controller.AppCaptchaVerifyController;
import com.site0.walnut.web2.controller.AppOtpSendController;
import com.site0.walnut.web2.controller.AppLoginModeController;
import com.site0.walnut.web2.controller.AppGetMyInfoController;
import com.site0.walnut.web2.controller.AppSysAjaxLogoutController;
import com.site0.walnut.web2.controller.AppLookupController;
import com.site0.walnut.web2.controller.ObjAncestorsController;
import com.site0.walnut.web2.controller.ObjChildrenController;
import com.site0.walnut.web2.controller.ObjContentController;
import com.site0.walnut.web2.controller.ObjFetch2Controller;
import com.site0.walnut.web2.controller.ObjFetchController;
import com.site0.walnut.web2.controller.ObjQueryController;
import com.site0.walnut.web2.controller.ObjSaveStreamController;
import com.site0.walnut.web2.controller.ObjSaveTextController;
import com.site0.walnut.web2.controller.ObjUpdateController;
import com.site0.walnut.web2.controller.SwitchSessionController;
import com.site0.walnut.web2.controller.WebRootController;
import com.site0.walnut.web2.controller.httpapi.HttpApiController;
import com.site0.walnut.web2.filter.atpath.SessionCheckFilter;
import com.site0.walnut.web2.filter.atpath.AsUsrFilter;
import com.site0.walnut.web2.support.WnLoginAuthLookup;

public final class WnRoutes {

    private WnRoutes() {}

    public static void register(WnMvcMapping _m) {
        WnMvcFilter[] none = new WnMvcFilter[0];
        _m.route(new WnMvcHttpMethod[]{WnMvcHttpMethod.GET},
                 "/u/ajax/chse",
                 SwitchSessionController.class,
                 none,
                 none);

        WnMvcHttpMethod[] _ANY = {WnMvcHttpMethod.ANY};
        // 2026-11-10 用户裁定：读操作入口收紧为 GET；控制器带 CORS 的附OPTIONS（保预检）。
        WnMvcHttpMethod[] _GET = {WnMvcHttpMethod.GET};

        // 准备过滤器
        WnMvcFilter[] session = {ctx -> new SessionCheckFilter(new WnLoginAuthLookup(ctx.services()
            .getLoginApi())).doFilter(ctx)};
        WnMvcFilter[] ajaxSession = {ctx -> new SessionCheckFilter(new WnLoginAuthLookup(ctx
            .services()
            .getLoginApi()), true, false).doFilter(ctx)};
        WnMvcFilter[] root = {ctx -> new AsUsrFilter(new WnLoginAuthLookup(ctx.services()
            .getLoginApi()), "root").doFilter(ctx)};

        // 路由 AppModule 模块
        _m.route(_ANY, "/a/login/**", AppLoginPageController.class, none, none);
        _m.route(_GET, "/a/open/?/**", AppOpenController.class, session, none);
        _m.route(_GET, "/a/load/?/**", AppLoadController.class, ajaxSession, none);
        _m.route(_ANY, "/a/beacon_run/**", AppBeaconRunController.class, ajaxSession, none);
        _m.route(_ANY, "/a/run/**", AppRunController.class, ajaxSession, none);
        _m.route(_ANY, "/a/sys_login_by_passwd", AppSysLoginByPasswdController.class, none, none);
        _m.route(_GET, "/a/sys_logout", AppSysLogoutController.class, none, none);
        _m.route(_ANY,
                 "/a/auth_login_by_domain_passwd",
                 AppAuthLoginByDomainPasswdController.class,
                 root,
                 none);
        _m.route(_ANY, "/a/captcha/get", AppCaptchaGetController.class, none, none);
        _m.route(_ANY, "/a/captcha/get/?", AppCaptchaGetController.class, none, none);
        _m.route(_ANY, "/a/captcha/verify", AppCaptchaVerifyController.class, none, none);
        _m.route(_ANY, "/a/captcha/verify/?", AppCaptchaVerifyController.class, none, none);
        _m.route(_ANY, "/a/otp/send", AppOtpSendController.class, none, none);
        _m.route(_ANY, "/a/otp/send/?", AppOtpSendController.class, none, none);
        _m.route(_GET, "/a/login_mode", AppLoginModeController.class, none, none);
        _m.route(_GET, "/a/me", AppGetMyInfoController.class, session, none);
        _m.route(_GET, "/a/sys_ajax_logout", AppSysAjaxLogoutController.class, none, none);
        _m.route(_GET, "/a/lookup/**", AppLookupController.class, none, none);

        // RM-NUTZ-08: Obj routes (10 个非废弃入口；6 个 @Deprecated 弃用不迁).
        // fetch/fetch2 = SessionCheck(true)(401)，其余 = SessionCheck()(302 重定向).
        // 2026-11-10
        // 用户裁定：读操作入口（fetch/fetch2/ancestors/children/content）收紧为 GET；
        // query 例外——查询条件复杂，支持 POST+GET（ANY）；find 弃用删除（2026-11-10）。
        // update/save 保持 ANY。
        _m.route(_GET, "/o/fetch", ObjFetchController.class, ajaxSession, none);
        _m.route(_GET, "/o/fetch2", ObjFetch2Controller.class, ajaxSession, none);
        _m.route(_GET, "/o/ancestors", ObjAncestorsController.class, session, none);
        _m.route(_ANY, "/o/update", ObjUpdateController.class, session, none);
        _m.route(_ANY, "/o/query", ObjQueryController.class, session, none);
        _m.route(_GET, "/o/children", ObjChildrenController.class, session, none);
        _m.route(_GET, "/o/content/**", ObjContentController.class, session, none);
        _m.route(_ANY, "/o/save/text", ObjSaveTextController.class, session, none);
        _m.route(_ANY, "/o/save/stream", ObjSaveStreamController.class, session, none);

        // RM-NUTZ-09: HttpApi routes.
        // /api/?/** = 旧 旧 HTTP API 模块 唯一入口；类级 WnAsUsr("root") → headFilter
        // AsUsrFilter("root").
        _m.route(_ANY, "/api/?/**", HttpApiController.class, root, none);

        // Matching is first-registered-first: the catch-all must stay last.
        _m.route(new WnMvcHttpMethod[]{WnMvcHttpMethod.ANY},
                 "/**",
                 WebRootController.class,
                 none,
                 none);
    }

}
