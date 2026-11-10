package com.site0.walnut.web2.router;

import com.site0.walnut.mvc.WnMvcFilter;
import com.site0.walnut.mvc.WnMvcHttpMethod;
import com.site0.walnut.mvc.WnMvcMapping;
import com.site0.walnut.web2.controller.AppLoginController;
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
import com.site0.walnut.web2.controller.ObjFindController;
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

    public static void register(WnMvcMapping mapping) {
        WnMvcFilter[] none = new WnMvcFilter[0];
        mapping.route(new WnMvcHttpMethod[]{WnMvcHttpMethod.GET},
                      "/u/ajax/chse", SwitchSessionController.class, none, none);

        WnMvcHttpMethod[] any = {WnMvcHttpMethod.ANY};
        WnMvcFilter[] session = {ctx -> new SessionCheckFilter(
                new WnLoginAuthLookup(ctx.services().getLoginApi())).doFilter(ctx)};
        WnMvcFilter[] ajaxSession = {ctx -> new SessionCheckFilter(
                new WnLoginAuthLookup(ctx.services().getLoginApi()), true, false).doFilter(ctx)};
        WnMvcFilter[] root = {ctx -> new AsUsrFilter(
                new WnLoginAuthLookup(ctx.services().getLoginApi()), "root").doFilter(ctx)};
        mapping.route(any, "/a/login", AppLoginController.class, none, none);
        mapping.route(any, "/a/login/**", AppLoginPageController.class, none, none);
        mapping.route(any, "/a/open/?/**", AppOpenController.class, session, none);
        mapping.route(any, "/a/load/?/**", AppLoadController.class, ajaxSession, none);
        mapping.route(any, "/a/beacon_run/**", AppBeaconRunController.class, ajaxSession, none);
        mapping.route(any, "/a/run/**", AppRunController.class, ajaxSession, none);
        mapping.route(any, "/a/sys_login_by_passwd", AppSysLoginByPasswdController.class, none, none);
        mapping.route(any, "/a/sys_logout", AppSysLogoutController.class, none, none);
        mapping.route(any, "/a/auth_login_by_domain_passwd", AppAuthLoginByDomainPasswdController.class, root, none);
        mapping.route(any, "/a/captcha/get", AppCaptchaGetController.class, none, none);
        mapping.route(any, "/a/captcha/get/?", AppCaptchaGetController.class, none, none);
        mapping.route(any, "/a/captcha/verify", AppCaptchaVerifyController.class, none, none);
        mapping.route(any, "/a/captcha/verify/?", AppCaptchaVerifyController.class, none, none);
        mapping.route(any, "/a/otp/send", AppOtpSendController.class, none, none);
        mapping.route(any, "/a/otp/send/?", AppOtpSendController.class, none, none);
        mapping.route(any, "/a/login_mode", AppLoginModeController.class, none, none);
        mapping.route(any, "/a/me", AppGetMyInfoController.class, none, none);
        mapping.route(any, "/a/sys_ajax_logout", AppSysAjaxLogoutController.class, none, none);
        mapping.route(any, "/a/lookup/**", AppLookupController.class, none, none);

        // JETTY-08: Obj routes (10 个非废弃入口；6 个 @Deprecated 弃用不迁).
        // fetch/fetch2 = SessionCheck(true)(401)，其余 = SessionCheck()(302 重定向).
        mapping.route(any, "/o/fetch", ObjFetchController.class, ajaxSession, none);
        mapping.route(any, "/o/fetch2", ObjFetch2Controller.class, ajaxSession, none);
        mapping.route(any, "/o/ancestors", ObjAncestorsController.class, session, none);
        mapping.route(any, "/o/update", ObjUpdateController.class, session, none);
        mapping.route(any, "/o/query", ObjQueryController.class, session, none);
        mapping.route(any, "/o/children", ObjChildrenController.class, session, none);
        mapping.route(any, "/o/find", ObjFindController.class, session, none);
        mapping.route(any, "/o/content/**", ObjContentController.class, session, none);
        mapping.route(any, "/o/save/text", ObjSaveTextController.class, session, none);
        mapping.route(any, "/o/save/stream", ObjSaveStreamController.class, session, none);

        // JETTY-09: HttpApi routes.
        // /api/?/** = 旧 HttpApiModule 唯一入口；类级 WnAsUsr("root") → headFilter AsUsrFilter("root").
        mapping.route(any, "/api/?/**", HttpApiController.class, root, none);

        // Matching is first-registered-first: the catch-all must stay last.
        mapping.route(new WnMvcHttpMethod[]{WnMvcHttpMethod.ANY},
                      "/**", WebRootController.class, none, none);
    }

}
