package com.site0.walnut.web.module.app;

import javax.servlet.http.HttpServletResponse;

import org.nutz.log.Log;
import org.nutz.mvc.View;
import org.nutz.mvc.view.HttpStatusView;
import org.nutz.mvc.view.RawView;
import org.nutz.mvc.view.ViewWrapper;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.impl.srv.WnBoxRunning;
import com.site0.walnut.login.WnLoginApi;
import com.site0.walnut.login.role.WnRoleList;
import com.site0.walnut.login.session.WnSession;
import com.site0.walnut.login.usr.WnUser;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.util.WnRun;
import com.site0.walnut.web.bean.WnApp;
import com.site0.walnut.web.impl.AppCheckAccess;
import com.site0.walnut.web.impl.WnAppService;

/**
 * 对应入口函数 <code>AppModule.open</code> 的服务类
 *
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnAppModuleOpen {

    private static final Log log = Wlog.getAPP();

    private WnAppService apps;

    private WnRun run;

    private String appName;

    private String objPath;

    private String objId;

    private String etag;

    private HttpServletResponse resp;

    public View invoke() {
        try {
            WnLoginApi auth = run.auth();
            // 得到应用
            WnApp app = apps.checkApp(appName, auth);

            if (log.isDebugEnabled()) {
                log.debugf("APP<%s>, ph=%s,id=%s,", appName, objPath, objId);
            }

            // 检查应用权限: root 组成员免查，可以打开任何 app
            WnSession se = app.getSession();
            WnObj oAppHome = app.getHome();
            WnUser me = se.getUser();
            WnRoleList roles = auth.roleLoader(se).getRoles(me);
            if (!roles.isMemberOfRole("root")) {
                WnIo io = run.io();
                WnObj oCheckAccess = io.fetch(oAppHome, "check_access.json");
                if (null != oCheckAccess) {
                    AppCheckAccess ca = io.readJson(oCheckAccess,
                                                    AppCheckAccess.class);
                    WnBoxRunning boxRunning = run.createRunning(false);
                    if (!ca.doCheck(io, se, auth, boxRunning)) {
                        return new HttpStatusView(403);
                    }
                }
            }

            // 渲染模板
            String html = apps.renderAppHtml(app);
            String sha1 = Wlang.sha1(html);
            if (etag != null && sha1.equals(etag)) {
                return new HttpStatusView(304);
            }
            resp.setHeader("ETag", sha1);
            return new ViewWrapper(new RawView("html"), html);
        }
        catch (Exception e) {
            if (log.isWarnEnabled()) {
                log.warn("Fail to open", e);
            }
            return HttpStatusView.HTTP_404;
        }
    }

    public WnAppService getApps() {
        return apps;
    }

    public void setApps(WnAppService apps) {
        this.apps = apps;
    }

    public WnRun getRun() {
        return run;
    }

    public void setRun(WnRun run) {
        this.run = run;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getObjPath() {
        return objPath;
    }

    public void setObjPath(String objPath) {
        this.objPath = objPath;
    }

    public String getObjId() {
        return objId;
    }

    public void setObjId(String objId) {
        this.objId = objId;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public HttpServletResponse getResp() {
        return resp;
    }

    public void setResp(HttpServletResponse resp) {
        this.resp = resp;
    }

}
