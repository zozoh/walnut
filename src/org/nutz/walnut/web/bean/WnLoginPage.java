package org.nutz.walnut.web.bean;

import javax.servlet.http.HttpServletResponse;

import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.mvc.View;
import org.nutz.mvc.view.HttpStatusView;
import org.nutz.mvc.view.RawView;
import org.nutz.mvc.view.ServerRedirectView;
import org.nutz.mvc.view.ViewWrapper;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.util.Wlog;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.web.view.WnObjDownloadView;

public class WnLoginPage {

    private static final Log log = Wlog.getAC();

    private WnIo io;
    private WnAccount domainUser;
    private String pageName;
    private String host;
    private String etag;
    private String range;
    private HttpServletResponse resp;

    public View genView(String rph) {
        // 登陆界面的主目录在哪里呢？
        WnObj oPageHome = null;

        // ...................................................
        // 映射自用户的域，用户有啥特殊配置没？
        if (null == pageName && null != domainUser) {
            String loginPath = domainUser.getMetaString("LOGIN_PAGE");
            if (!Strings.isBlank(loginPath)) {
                // 直接就是重定向
                if (loginPath.matches("^(https?:)?//.+$")) {
                    return new ServerRedirectView(loginPath);
                }
                // 那么就是一个文件夹
                else {
                    NutMap vars = Lang.map("HOME", domainUser.getHomePath());
                    String aph = Wn.normalizeFullPath(loginPath, vars);
                    oPageHome = io.fetch(null, aph);
                    if (null == oPageHome) {
                        log.warnf("login page lost '%s' @(%s)", loginPath, domainUser.getName());
                    }
                    if (!oPageHome.isDIR()) {
                        oPageHome = null;
                        log.warnf("login page NoDir '%s' @(%s)", loginPath, domainUser.getName());
                    }
                }
            }

        }

        // ...................................................
        // 看看 /etc/hosts.d/ 有木有指定登录界面
        if (!"sys".equals(pageName) && null == oPageHome) {
            WnObj oHosts = io.fetch(null, "/etc/hosts.d");
            if (null != oHosts) {
                if (!Strings.isBlank(host)) {
                    oPageHome = io.fetch(oHosts, host + "/pages");
                }
                // 默认的域名为 default
                if (null == oPageHome) {
                    oPageHome = io.fetch(oHosts, "default/pages");
                }
            }
        }

        // ...................................................
        // 没有就采用默认的
        if (null == oPageHome) {
            return this.getBuiltinLoginView();
        }

        // ...................................................
        // 得到文件内容
        rph = Strings.sBlank(rph, "index.html");
        WnObj o = io.fetch(oPageHome, rph);

        // 木有就 404
        if (null == o) {
            return new HttpStatusView(404);
        }

        // 显示内容
        return new WnObjDownloadView(io, o, null, null, etag, range);
    }

    private static final String LOGIN_HTML;
    private static final String LOGIN_SHA1;

    static {
        LOGIN_HTML = Files.read("org/nutz/walnut/web/module/login.html");
        LOGIN_SHA1 = Lang.sha1(LOGIN_HTML);
    }

    public View getBuiltinLoginView() {
        if (etag != null && LOGIN_SHA1.equals(etag)) {
            return new HttpStatusView(304);
        }

        resp.setHeader("ETag", LOGIN_SHA1);
        return new ViewWrapper(new RawView("html"), LOGIN_HTML);
    }

    public WnIo getIo() {
        return io;
    }

    public void setIo(WnIo io) {
        this.io = io;
    }

    public String getPageName() {
        return pageName;
    }

    public void setPageName(String pageName) {
        this.pageName = pageName;
    }

    public WnAccount getDomainUser() {
        return domainUser;
    }

    public void setDomainUser(WnAccount domainUser) {
        this.domainUser = domainUser;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }

    public HttpServletResponse getResp() {
        return resp;
    }

    public void setResp(HttpServletResponse resp) {
        this.resp = resp;
    }

}
