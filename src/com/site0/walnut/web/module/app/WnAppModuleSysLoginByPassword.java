package com.site0.walnut.web.module.app;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.lang.Strings;
import org.nutz.log.Log;
import org.nutz.mvc.View;
import org.nutz.mvc.view.ServerRedirectView;
import org.nutz.mvc.view.ViewWrapper;
import org.nutz.web.WebException;
import org.nutz.web.ajax.Ajax;
import org.nutz.web.ajax.AjaxView;

import com.site0.walnut.login.WnLoginApi;
import com.site0.walnut.login.session.WnSession;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.web.WnConfig;
import com.site0.walnut.web.module.AbstractWnModule;
import com.site0.walnut.web.util.WnWeb;
import com.site0.walnut.web.view.WnAddCookieViewWrapper;

import redis.clients.jedis.exceptions.JedisException;

/**
 * 对应入口函数 <code>AppModule.sys_login_by_passwd</code> 的服务类
 *
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnAppModuleSysLoginByPassword {

    private static final Log log = Wlog.getAPP();

    private AbstractWnModule module;

    private WnLoginApi auth;

    private WnConfig conf;

    private String name;

    private String passwd;

    private boolean ajax;

    private String referer;

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
        // 尝试用系统账号登录
        try {
            WnSession se = auth.loginByPassword(name, passwd);
            // 尝试初始化会话
            module.try_run_profile(se);
            // 如果是 Ajax 视图
            if (ajax) {
                // 获取 cookie 模板
                Object reo = se.toBean(auth);
                return new ViewWrapper(new WnAddCookieViewWrapper(conf,
                                                                  new AjaxView(),
                                                                  null),
                                       reo);
            }
            // 直接跳转到用户的主应用
            return this.__get_session_default_view(se);
        }
        // 出错了
        catch (WebException e) {
            Throwable eCause = e.getCause();
            if (null != eCause && (eCause instanceof JedisException)) {
                if (log.isWarnEnabled()) {
                    log.warn("Jedis error", eCause);
                }
            }
            Object reo = Ajax.fail()
                .setErrCode(e.getKey())
                .setData(e.getReason());
            // 返回视图
            if (ajax) {
                return new ViewWrapper(new AjaxView(), reo);
            }
            // 返回到原先地址
            String redirectTo = Strings.sBlank(referer, "/");
            return new ServerRedirectView(redirectTo);
        }
    }

    private View __get_session_default_view(WnSession se) {
        String appName = se.getEnv().getString("OPEN", "wn.console");
        String url = "/a/open/" + appName;
        Object reData = se.toBean(auth);
        return new ViewWrapper(new WnAddCookieViewWrapper(conf, url), reData);
    }

    public AbstractWnModule getModule() {
        return module;
    }

    public void setModule(AbstractWnModule module) {
        this.module = module;
    }

    public WnLoginApi getAuth() {
        return auth;
    }

    public void setAuth(WnLoginApi auth) {
        this.auth = auth;
    }

    public WnConfig getConf() {
        return conf;
    }

    public void setConf(WnConfig conf) {
        this.conf = conf;
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

    public String getReferer() {
        return referer;
    }

    public void setReferer(String referer) {
        this.referer = referer;
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
