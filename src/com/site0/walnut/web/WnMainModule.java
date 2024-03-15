package com.site0.walnut.web;

import java.io.IOException;

import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Files;
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
import org.nutz.web.ajax.AjaxViewMaker;

import com.site0.walnut.WnVersion;
import com.site0.walnut.api.auth.WnAuthSession;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.WnSysRuntime;
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
    @Ok(">>:${obj}")
    public String doCheck() {
        // 看看有木有会话
        WnAuthSession se = null;

        if (Wn.WC().hasTicket()) {
            String ticket = Wn.WC().getTicket();
            se = auth().getSession(ticket);

            // 有会话的话，转到默认打开应用
            if (null != se && !se.isDead()) {
                String appName = se.getVars().getString("OPEN", "wn.console");
                return "/a/open/" + appName;
            }
        }

        // 木有会话的话，看看重定向到哪里，默认的，应该是 /a/login/
        return conf.getSysEntryUrl();
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
