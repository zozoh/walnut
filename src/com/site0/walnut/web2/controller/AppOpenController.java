package com.site0.walnut.web2.controller;

import org.nutz.lang.util.NutMap;
import com.site0.walnut.log.WnLog;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.impl.srv.WnBoxRunning;
import com.site0.walnut.login.WnLoginApi;
import com.site0.walnut.login.role.WnRoleList;
import com.site0.walnut.login.session.WnSession;
import com.site0.walnut.login.usr.WnUser;
import com.site0.walnut.mvc.WnMvcContext;
import com.site0.walnut.mvc.WnMvcView;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.api.bean.WnApp;
import com.site0.walnut.api.bean.AppCheckAccess;
import com.site0.walnut.web2.controller.app.WnAppService;

public class AppOpenController extends AbstractAppController {

    private static final WnLog log = Wlog.getAPP();

    @Override
    public WnMvcView handle(WnMvcContext ctx) {
        // 2026-11-10 用户裁定：GET-only，参数只从 query string 取（不读 body form）
        NutMap params = ctx.request().queryStringMap();
        WnAppService apps = services(ctx).getAppService();
        WnIo io = io(ctx);
        WnLoginApi auth = auth(ctx);
        String appName = arg(ctx, 0);
        String objPath = params.getString("ph");
        String objId = params.getString("id");
        String etag = ctx.request().getHeader("If-None-Match");
        try {
            WnApp app = apps.checkApp(appName, auth);
            if (log.isDebugEnabled())
                log.debugf("APP<%s>, ph=%s,id=%s,", appName, objPath, objId);
            WnSession se = app.getSession();
            WnObj oAppHome = app.getHome();
            WnUser me = se.getUser();
            WnRoleList roles = auth.roleLoader(se).getRoles(me);
            if (!roles.isMemberOfRole("root")) {
                WnObj oCheckAccess = io.fetch(oAppHome, "check_access.json");
                if (null != oCheckAccess) {
                    AppCheckAccess ca = io.readJson(oCheckAccess, AppCheckAccess.class);
                    WnBoxRunning boxRunning = apps.createRunning(false);
                    if (!ca.doCheck(io, se, auth, boxRunning))
                        return WnMvcView.status(403);
                }
            }
            String html = apps.renderAppHtml(app);
            String sha1 = Wlang.sha1(html);
            if (etag != null && sha1.equals(etag))
                return WnMvcView.status(304);
            ctx.response().header("ETag", sha1);
            return WnMvcView.html(html);
        }
        catch (Exception e) {
            if (log.isWarnEnabled())
                log.warn("Fail to open", e);
            return WnMvcView.status(404);
        }
    }
}
