package org.nutz.walnut.web;

import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.ioc.loader.json.JsonLoader;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.ChainBy;
import org.nutz.mvc.annotation.IocBy;
import org.nutz.mvc.annotation.Localization;
import org.nutz.mvc.annotation.Modules;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.SetupBy;
import org.nutz.mvc.annotation.Views;
import org.nutz.mvc.ioc.provider.ComboIocProvider;
import org.nutz.mvc.ioc.provider.JsonIocProvider;
import org.nutz.walnut.api.usr.WnSession;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.web.module.AbstractWnModule;
import org.nutz.walnut.web.view.WnViewMaker;
import org.nutz.web.WebException;
import org.nutz.web.ajax.AjaxViewMaker;

@SetupBy(WnSetup.class)
// @IocBy(type = ComboIocProvider.class,
// args = {"*org.nutz.ioc.loader.json.JsonLoader",
// "ioc",
// "*org.nutz.ioc.loader.annotation.AnnotationIocLoader",
// "org.nutz.walnut",
// "$dynamic"})
// @Modules(scanPackage = true, packages = {"org.nutz.walnut.web.module",
// "$dynamic"})
@IocBy(type = JsonIocProvider.class, args = {"ioc"})
@Modules(by = "ioc:webscan")
@Localization("msg")
@Views({AjaxViewMaker.class, WnViewMaker.class})
@ChainBy(args = {"chain/walnut-chain.js"})
@IocBean
public class WnMainModule extends AbstractWnModule {

    @Inject("java:$conf.get('main-app', 'console')")
    private String mainApp;

    @At("/version")
    @Ok("jsp:jsp.show_text")
    public String version() {
        return "1.0" + io.toString();
    }

    @At("/")
    @Ok(">>:${obj}")
    public String doCheck() {
        String seid = Wn.WC().SEID();
        if (null == seid)
            return "/u/login";

        try {
            WnSession se = sess.check(seid);

            // 记录到上下文
            Wn.WC().SE(se);
            Wn.WC().me(se.me(), se.group());

            // 查看会话环境变量，看看需要转到哪个应用
            String appPath = se.envs().getString("OPEN", mainApp);

            // 那么 Session 木有问题了
            return "/a/open/" + appPath;
        }
        catch (WebException e) {
            return "/u/login";
        }

    }
}
