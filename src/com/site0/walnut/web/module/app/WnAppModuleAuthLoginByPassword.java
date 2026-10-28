package com.site0.walnut.web.module.app;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.lang.util.NutBean;
import org.nutz.log.Log;
import org.nutz.mvc.View;
import org.nutz.mvc.view.ServerRedirectView;
import org.nutz.mvc.view.ViewWrapper;
import org.nutz.web.ajax.AjaxView;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.login.WnLoginApi;
import com.site0.walnut.login.WnLoginApiMaker;
import com.site0.walnut.login.WnLoginOptions;
import com.site0.walnut.login.session.WnSession;
import com.site0.walnut.login.site.WnLoginSite;
import com.site0.walnut.login.usr.WnUser;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.util.Wn;
import com.site0.walnut.web.WnConfig;
import com.site0.walnut.web.module.AbstractWnModule;
import com.site0.walnut.web.util.WnWeb;
import com.site0.walnut.web.view.WnAddCookieViewWrapper;

/**
 * 对应入口函数 <code>AppModule.auth_login_by_domain_passwd</code> 的服务类
 *
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnAppModuleAuthLoginByPassword {

    private static final Log log = Wlog.getAPP();

    private AbstractWnModule module;

    private WnIo io;

    private WnConfig conf;

    private String siteId;

    private String name;

    private String passwd;

    private boolean ajax;

    private String hostName;

    private HttpServletRequest req;

    private HttpServletResponse resp;

    public View invoke() {
        // 允许跨域
        WnWeb.setCrossDomainHeaders("*", (headName, headValue) -> {
            resp.setHeader(WnWeb.niceHeaderName(headName), headValue);
        });
        // 对于 options 放过
        if (WnWeb.isRequestOptions(req)) {
            return null;
        }
        // 获取登录接口
        Object reo = null;
        WnLoginSite site = WnLoginSite.create(io, siteId, hostName);
        // -------------------------------------------------
        // 找不到站点，那么一定要抛错
        View view = null;
        if (null == site) {
            if (ajax) {
                view = new AjaxView();
            } else {
                view = new ServerRedirectView("/");
            }
            if (log.isWarnEnabled()) {
                log.warnf("e.auth.login.domain_without_www: %s @ %s",
                          siteId,
                          hostName);
            }
            reo = Er.create("e.auth.login.domain_without_www");
            // 包裹返回
            return new ViewWrapper(view, reo);
        }
        // -------------------------------------------------
        // 如果这个域声明了默认登录站点，那么则试图用这个站点的账户系统登录
        WnSession se = null;
        String redirectPath = "/";
        try {
            // 如果采用域用户登陆，则校验系统账户
            // 并返回 CookieView
            if (site.isDomainUser(name)) {
                WnLoginApi auth = module.auth();
                if (log.isInfoEnabled()) {
                    log.infof("Login as domain-user: %s", name);
                }
                se = auth.loginByPassword(name, passwd);
                String appName = se.getEnv().getString("OPEN", "wn-term");
                redirectPath = "/a/open/" + appName;
                reo = se.toBean(auth);
            }
            // 采用域用户库来登陆
            else {
                NutBean env = site.getSessionVarsBySiteHome();
                WnLoginOptions options = site.getOptions();
                WnLoginApi auth = WnLoginApiMaker.forHydrate()
                    .make(io, env, options);
                if (log.isInfoEnabled()) {
                    log.infof("Login as sub-user: %s", name);
                }
                WnUser user = auth.checkUser(name);
                if (log.isInfoEnabled()) {
                    log.infof("sub user : %s", user.toString());
                }
                // -----------------------------------------
                // 检查登录密码，看看是否登录成功
                if (user.isMatchedRawPasswd(passwd)) {
                    if (log.isInfoEnabled()) {
                        log.infof("OK: check passwd");
                    }

                    // 获取会话时长设置
                    int se_du = auth.getSessionDuration();

                    // 注册新会话
                    se = auth.createSession(user, Wn.SET_LOGIN_APP, se_du);

                    // 确保用户是可以访问域主目录的
                    site.assertHomeAccessable(se);
                    if (log.isInfoEnabled()) {
                        log.infof("OK: check_home_accessable");
                    }

                    // 更新会话元数据
                    if (log.isInfoEnabled()) {
                        log.infof("OK: create session: %s : %s",
                                  se.getTicket(),
                                  se.getMyName());
                    }

                    // 获取重定向路径
                    String appName = se.getEnv()
                        .getString("OPEN", "wn.manager");
                    redirectPath = "/a/open/" + appName;

                    if (log.isInfoEnabled()) {
                        log.infof(">>: %s", redirectPath);
                    }

                    // 准备返回值
                    reo = se.toBean(auth);
                }
                // -----------------------------------------
                // 登录失败
                else {
                    if (log.isInfoEnabled()) {
                        log.infof("KO: passwd fail");
                    }
                    reo = Er.create("e.auth.login.invalid.passwd");
                }
            }

            // 尝试初始化会话
            module.try_run_profile(se);

            // 根据选项包裹返回视图
            // AJAX 视图
            if (ajax) {
                view = new WnAddCookieViewWrapper(conf, new AjaxView(), null);
            }
            // 重定向视图
            else {
                view = new WnAddCookieViewWrapper(conf, redirectPath);
            }
            // 返回
            return new ViewWrapper(view, reo);
        }
        // 通常是账户不存在或者权限错误，进入这个分支
        catch (Exception e) {
            if (log.isWarnEnabled()) {
                log.warn(e.toString(), e);
            }
            reo = e;
        }
        // -----------------------------------------
        // 进行到这里一定出现了错误，这里准备一下错误视图
        if (ajax) {
            view = new AjaxView();
        } else {
            view = new ServerRedirectView(redirectPath);
        }
        // -----------------------------------------
        // 包裹返回
        return new ViewWrapper(view, reo);
    }

    public AbstractWnModule getModule() {
        return module;
    }

    public void setModule(AbstractWnModule module) {
        this.module = module;
    }

    public WnIo getIo() {
        return io;
    }

    public void setIo(WnIo io) {
        this.io = io;
    }

    public WnConfig getConf() {
        return conf;
    }

    public void setConf(WnConfig conf) {
        this.conf = conf;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }

    public boolean isAjax() {
        return ajax;
    }

    public void setAjax(boolean ajax) {
        this.ajax = ajax;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public HttpServletRequest getReq() {
        return req;
    }

    public void setReq(HttpServletRequest req) {
        this.req = req;
    }

    public HttpServletResponse getResp() {
        return resp;
    }

    public void setResp(HttpServletResponse resp) {
        this.resp = resp;
    }

}
