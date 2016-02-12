package org.nutz.walnut.web;

import org.nutz.ioc.loader.annotation.Inject;
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
import org.nutz.mvc.ioc.provider.JsonIocProvider;
import org.nutz.walnut.api.usr.WnSession;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.web.module.AbstractWnModule;
import org.nutz.walnut.web.view.WnViewMaker;
import org.nutz.web.WebException;
import org.nutz.web.ajax.AjaxViewMaker;

@SetupBy(WnSetup.class)
@IocBy(type = JsonIocProvider.class, args = {"ioc"}, init = {"loader"})
@Modules(by = "ioc:webscan")
@Localization("msg")
@Views({AjaxViewMaker.class, WnViewMaker.class})
@ChainBy(args = {"chain/walnut-chain.js"})
@IocBean
@Encoding(input = "UTF-8", output = "UTF-8")
public class WnMainModule extends AbstractWnModule {

    // 跳转到homePage吗?还是loginPage
    @Inject("java:$conf.getBoolean('use-homepage','false')")
    private boolean useHomePage;

    @Inject("java:$conf.get('page-home','home')")
    private String page_home;

    @At("/version")
    @Ok("jsp:jsp.show_text")
    public String version() {
        return "1.0" + io.toString();
    }

    @At("/")
    @Ok(">>:${obj}")
    public String doCheck() {
        String seid = Wn.WC().SEID();
        if (null == seid) {
            if (useHomePage) {
                return page_home;
            }
            return "/u/login";
        }

        try {
            WnSession se = sess.check(seid);

            // 记录到上下文
            Wn.WC().SE(se);
            Wn.WC().me(se.me(), se.group());

            // 查看会话环境变量，看看需要转到哪个应用
            String appPath = se.vars().getString("OPEN");

            // 那么 Session 木有问题了
            return "/a/open/" + appPath;
        }
        catch (WebException e) {
            e.printStackTrace();
            if (useHomePage) {
                return page_home;
            }
            return "/u/login";
        }

    }
}
