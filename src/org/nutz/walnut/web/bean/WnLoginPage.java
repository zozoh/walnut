package org.nutz.walnut.web.bean;

import javax.servlet.http.HttpServletResponse;

import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.mvc.View;
import org.nutz.mvc.view.HttpStatusView;
import org.nutz.mvc.view.RawView;
import org.nutz.mvc.view.ViewWrapper;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.web.view.WnObjDownloadView;

public class WnLoginPage {

    private WnIo io;
    private String host;
    private String etag;
    private String range;
    private HttpServletResponse resp;

    public WnLoginPage(WnIo io, String host, String etag, String range, HttpServletResponse resp) {
        this.io = io;
        this.host = host;
        this.etag = etag;
        this.range = range;
        this.resp = resp;
    }

    public View genView(String rph) {
        // 登陆界面的主目录在哪里呢？
        WnObj oPageHome = null;

        // 看看有没有配置目录
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

        // 没有就采用默认的
        if (null == oPageHome) {
            return this.getBuiltinLoginView();
        }

        // 得到文件内容
        WnObj o = io.fetch(oPageHome, rph);

        if (null == o) {
            return new HttpStatusView(404);
        }

        return new WnObjDownloadView(io, o, null, etag, range);
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

}
