package com.site0.walnut.web;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Files;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.View;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Attr;
import org.nutz.mvc.annotation.ChainBy;
import org.nutz.mvc.annotation.Encoding;
import org.nutz.mvc.annotation.IocBy;
import org.nutz.mvc.annotation.Localization;
import org.nutz.mvc.annotation.Modules;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.ReqHeader;
import org.nutz.mvc.annotation.SetupBy;
import org.nutz.mvc.annotation.Views;
import org.nutz.mvc.ioc.provider.ComboIocProvider;
import org.nutz.mvc.view.ServerRedirectView;
import org.nutz.web.ajax.AjaxViewMaker;

import com.site0.walnut.WnVersion;
import com.site0.walnut.api.auth.WnAuthSession;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.WnSysRuntime;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.Wsum;
import com.site0.walnut.util.bean.WnSummaryData;
import com.site0.walnut.web.module.AbstractWnModule;
import com.site0.walnut.web.setup.WnSetup;
import com.site0.walnut.web.view.WnObjDownloadView;
import com.site0.walnut.web.view.WnViewMaker;

@SetupBy(WnSetup.class)
@IocBy(type = ComboIocProvider.class, args = {"*js", "ioc"}, init = {"loader"})
@Modules(by = "ioc:webscan")
@Localization("msg")
@Views({AjaxViewMaker.class, WnViewMaker.class})
@ChainBy(args = {"chain/walnut-chain.js"})
@IocBean
@Encoding(input = "UTF-8", output = "UTF-8")
public class WnMainModule extends AbstractWnModule {

    private static WnSummaryData DFT_FAVICON = new WnSummaryData();

    // private static Map<String, WnSummaryData> FAVICONS = new Hashtable<>();

    static {
        byte[] faviconData = Files.readBytes("com/site0/walnut/web/favicon.ico");
        DFT_FAVICON.setData(faviconData);
        DFT_FAVICON.setSha1(Wsum.sha1AsString(faviconData));
    }

    @At("/")
    public View doCheck(@ReqHeader("If-None-Match") String etag,
                        @ReqHeader("Range") String range,
                        HttpServletRequest req) {
        // 这里增加一个处理 C 记录的逻辑
        if ("yes".equals(req.getAttribute("wn_www_static"))) {
            return handleRecordC("", etag, range, req);
        }

        // 看看有木有会话
        WnAuthSession se = null;

        if (Wn.WC().hasTicket()) {
            String ticket = Wn.WC().getTicket();
            se = auth().getSession(ticket);

            // 有会话的话，转到默认打开应用
            if (null != se && !se.isDead()) {
                String appName = se.getVars().getString("OPEN", "wn.console");
                return new ServerRedirectView("/a/open/" + appName);
            }
        }

        // 木有会话的话，看看重定向到哪里，默认的，应该是 /a/login/
        String entryUrl = conf.getSysEntryUrl();
        return new ServerRedirectView(entryUrl);
    }

    @At("/**")
    public View getSiteContent(String reqPath,
                               @ReqHeader("If-None-Match") String etag,
                               @ReqHeader("Range") String range,
                               HttpServletRequest req) {
        // 这里增加一个处理 C 记录的逻辑
        if ("yes".equals(req.getAttribute("wn_www_static"))) {
            return handleRecordC(reqPath, etag, range, req);
        }

        throw Wlang.makeThrow("Invalid Req: %s", reqPath);
    }

    private View handleRecordC(String reqPath, String etag, String range, HttpServletRequest req) {
        // 获取域，譬如 xyz 表示 /home/xyz 这个域
        String domain = req.getAttribute("wn_www_grp").toString();
        // 获取站点的静态资源目录 ~/www/mysite
        String sitePh = req.getAttribute("wn_www_site").toString();

        // 处理一下目录
        String homePath = "/home/" + domain;
        NutMap vars = Wlang.map("HOME", homePath);
        vars.put("PWD", homePath);
        String dirPath = Wn.normalizeFullPath(sitePh, vars);
        WnIo io = io();
        WnObj oDir = io.check(null, dirPath);

        // 获取要访问的资源，默认就是 index.html
        String path = null == reqPath ? "" : reqPath;
        while (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (Ws.isBlank(path)) {
            path = "index.html";
        }
        WnObj obj = io.fetch(oDir, path);
        if (null == obj) {
            return HttpStatusView(404, path);
        }
        return new WnObjDownloadView(io, obj, null, null, etag, range);
    }

    private View HttpStatusView(int i, String path) {
        return null;
    }

    @At("/favicon")
    public View getFavicon(@Attr("wn_www_grp") String domain,
                           @ReqHeader("If-None-Match") String etag,
                           @ReqHeader("Range") String range)
            throws IOException {
        // 看看站点有木有特殊的 ICON

        // 采用内置的 ICON
        return new WnObjDownloadView(DFT_FAVICON.getData(),
                                     DFT_FAVICON.getSha1(),
                                     null,
                                     "image/x-icon",
                                     null,
                                     etag,
                                     range);
    }

    @At("/u/version")
    @Ok("raw")
    public String version() {
        return WnVersion.getName();
    }

    @At("/u/runtime")
    @Ok("json")
    public WnSysRuntime runtime() {
        return Wn.getRuntime();
    }
}
