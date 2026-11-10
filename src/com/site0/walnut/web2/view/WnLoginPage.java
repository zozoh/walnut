package com.site0.walnut.web2.view;

import org.nutz.lang.Encoding;
import org.nutz.lang.Files;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.Strings;
import com.site0.walnut.util.tmpl.WnTmpl;
import com.site0.walnut.util.tmpl.WnTmplX;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import com.site0.walnut.mvc.WnMvcView;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.login.usr.WnUser;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;

public class WnLoginPage {

    private static final Log log = Wlog.getAC();

    private WnIo io;
    private WnUser domainUser;
    private NutBean config;
    private String pageName;
    private String host;
    private String sitePath;
    private String etag;
    private String range;

    public WnMvcView genView(String rph) {
        // 登陆界面的主目录在哪里呢？
        WnObj oPageHome = null;

        // ...................................................
        // 映射自用户的域，用户有啥特殊配置没？
        if (null == pageName && null != domainUser) {
            String loginPath = sitePath;
            if (Ws.isBlank(loginPath)) {
                loginPath = domainUser.getMetaString("LOGIN_PAGE");
            }
            if (!Strings.isBlank(loginPath)) {
                // 直接就是重定向
                if (loginPath.matches("^(https?:)?//.+$")) {
                    return WnMvcView.redirect(loginPath);
                }
                // 那么就是一个文件夹
                else {
                    NutMap vars = Wlang.map("HOME", domainUser.getHomePath());
                    String aph = Wn.normalizeFullPath(loginPath, vars);
                    oPageHome = io.fetch(null, aph);
                    if (null == oPageHome) {
                        log.warnf("login page lost '%s' @(%s)",
                                  loginPath,
                                  domainUser.getName());
                    }
                    if (!oPageHome.isDIR()) {
                        oPageHome = null;
                        log.warnf("login page NoDir '%s' @(%s)",
                                  loginPath,
                                  domainUser.getName());
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
            NutBean c = _gen_render_vars();
            return this.getBuiltinLoginView(c);
        }

        // ...................................................
        // 得到文件内容
        rph = Strings.sBlank(rph, "index.html");
        WnObj o = io.fetch(oPageHome, rph);

        // 木有就 404
        if (null == o) {
            return WnMvcView.status(404);
        }

        // 如果是 HTML 需要执行一下动态模板
        if (o.isType("html")) {
            String str = io.readText(o);
            WnTmpl tmpl = WnTmpl.parse(str);
            NutBean c = _gen_render_vars();
            String html = tmpl.render(c);
            byte[] buf = html.getBytes(Encoding.CHARSET_UTF8);
            return new WnDownloadView(buf,
                                      null,
                                      "text/html",
                                      o.name(),
                                      o.sha1(),
                                      null);
        }

        // 显示内容
        return new WnDownloadView(io, o, null, null, etag, range);
    }

    private NutBean _gen_render_vars() {
        NutBean c = Wn.getSysConfMap(io);
        if (null != this.domainUser) {
            c.putAll(domainUser.toBean());
        }
        if (null != domainUser) {
            c.put("DOMAIN", domainUser.getMainGroup());
        } else {
            c.put("DOMAIN", "_default_");
        }
        if (null != this.config) {
            c.putAll(this.config);
        }
        return c;
    }

    private static final String LOGIN_HTML;
    private static final WnTmplX LOGIN_TMPL;
    // private static final String LOGIN_SHA1;

    static {
        LOGIN_HTML = Files.read("com/site0/walnut/web/module/login.html");
        LOGIN_TMPL = WnTmplX.parse(LOGIN_HTML);
        // LOGIN_SHA1 = Wlang.sha1(LOGIN_HTML);
    }

    public WnMvcView getBuiltinLoginView(NutBean vars) {
        // zozoh@20261103: 永远动态生成，因为需要动态获取登录模式
        // 以后可能需要更多的动态信息
        // if (etag != null && LOGIN_SHA1.equals(etag)) {
        // return WnMvcView.status(304);
        // }

        // resp.header("ETag", LOGIN_SHA1);
        String html = LOGIN_TMPL.render(vars);
        return WnMvcView.html(html);
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

    public WnUser getDomainUser() {
        return domainUser;
    }

    public void setDomainUser(WnUser domainUser) {
        this.domainUser = domainUser;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getSitePath() {
        return sitePath;
    }

    public void setSitePath(String sitePath) {
        this.sitePath = sitePath;
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

    public NutBean getConfig() {
        return config;
    }

    public void setConfig(NutBean config) {
        this.config = config;
    }

}
