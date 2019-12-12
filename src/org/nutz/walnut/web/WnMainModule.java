package org.nutz.walnut.web;

import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.Scope;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Attr;
import org.nutz.mvc.annotation.ChainBy;
import org.nutz.mvc.annotation.Encoding;
import org.nutz.mvc.annotation.IocBy;
import org.nutz.mvc.annotation.Localization;
import org.nutz.mvc.annotation.Modules;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.SetupBy;
import org.nutz.mvc.annotation.Views;
import org.nutz.mvc.ioc.provider.ComboIocProvider;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.auth.WnAuthSession;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.web.module.AbstractWnModule;
import org.nutz.walnut.web.view.WnViewMaker;
import org.nutz.web.WebException;
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

    private static Log log = Logs.get();

    // zozoh 删除吧，木有用了，有定制化的登录界面机制了
    // 跳转到homePage吗?还是loginPage
    // @Inject("java:$conf.getBoolean('use-homepage','false')")
    // private boolean useHomePage;
    //
    // @Inject("java:$conf.get('page-home','home')")
    // private String page_home;

    @Inject("java:$conf.entryPages")
    private NutMap entryPageMap;

    private String _get_entry_page_url(String host) {
        String re = entryPageMap.getString(host);
        if (Strings.isBlank(re))
            re = entryPageMap.getString("default", "/u/h/login.html");
        return re;
    }

    @At("/")
    @Ok(">>:${obj}")
    public String doCheck(@Attr(value = "wn_www_host", scope = Scope.REQUEST) String host) {
        if (!Wn.WC().hasTicket()) {
            return this._get_entry_page_url(host);
        }

        try {
            String ticket = Wn.WC().getTicket();
            WnAuthSession se = auth.checkSession(ticket);

            if (null == se || se.isDead()) {
                return this._get_entry_page_url(host);
            }

            Wn.WC().setSession(se);
            WnAccount me = se.getMe();

            // 如果当前用户的 ID 和名字相等，则必须强迫其改个名字
            if (me.isNameSameAsId()) {
                return "/u/h/rename.html";
            }

            // 查看会话环境变量，看看需要转到哪个应用
            String appPath = se.getVars().getString("OPEN", "wn.console");

            // 那么 Session 木有问题了
            return "/a/open/" + appPath;
        }
        catch (WebException e) {
            if (log.isInfoEnabled())
                log.info(e.toString());
            // return "/u/h/login.html";
            return this._get_entry_page_url(host);
        }

    }
}
