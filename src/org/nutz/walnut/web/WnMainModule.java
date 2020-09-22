package org.nutz.walnut.web;

import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.ChainBy;
import org.nutz.mvc.annotation.Encoding;
import org.nutz.mvc.annotation.IocBy;
import org.nutz.mvc.annotation.Localization;
import org.nutz.mvc.annotation.Modules;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.SetupBy;
import org.nutz.mvc.annotation.Views;
import org.nutz.mvc.ioc.provider.ComboIocProvider;
import org.nutz.walnut.WnVersion;
import org.nutz.walnut.api.auth.WnAuthSession;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnSysRuntime;
import org.nutz.walnut.web.module.AbstractWnModule;
import org.nutz.walnut.web.setup.WnSetup;
import org.nutz.walnut.web.view.WnViewMaker;
import org.nutz.web.ajax.AjaxViewMaker;

@SetupBy(WnSetup.class)
@IocBy(type = ComboIocProvider.class, args = {"*js", "ioc"}, init = {"loader"})
@Modules(by = "ioc:webscan")
@Localization("msg")
@Views({AjaxViewMaker.class, WnViewMaker.class})
@ChainBy(args = {"chain/walnut-chain.js"})
@IocBean
@Encoding(input = "UTF-8", output = "UTF-8")
public class WnMainModule extends AbstractWnModule {

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
