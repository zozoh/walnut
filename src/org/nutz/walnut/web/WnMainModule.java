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
import org.nutz.walnut.api.auth.WnAuthSession;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.web.module.AbstractWnModule;
import org.nutz.walnut.web.setup.WnSetup;
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

    @Inject("java:$conf.getLong('rt-ep-cache-du', 300000)")
    private long rt_ep_cache_du;

    /**
     * 运行时的各个域入口页（缓存时间参见上面的配置）
     */
    private NutMap runtimeEntryPageMap;

    /**
     * 最后一次加载运行时入口页表的时间（ms）
     */
    private long rt_ep_load_time;

    private void _reload_runtime_entry_pageMap() {
        WnObj o = io().fetch(null, "/etc/hosts.d/entry_url");
        if (null != o) {
            this.runtimeEntryPageMap = io().readJson(o, NutMap.class);
        } else {
            this.runtimeEntryPageMap = null;
        }
        this.rt_ep_load_time = Wn.now();
    }

    private void _check_runtime_entry_pageMap() {
        long now = Wn.now();
        long du = now - this.rt_ep_load_time;
        // 采用系统运行时 URL 映射
        if (null != this.runtimeEntryPageMap) {
            // 太久没更新了，更新一下
            if (du > this.rt_ep_cache_du) {
                _reload_runtime_entry_pageMap();
            }
        }
        // 没读取过，或者超过5分钟没读取过，那么再读取一下咯
        else if (this.rt_ep_load_time <= 0 || du > this.rt_ep_cache_du) {
            _reload_runtime_entry_pageMap();
        }
    }

    private String _get_entry_page_url(String host) {
        // 检查一下动态入口映射表
        _check_runtime_entry_pageMap();

        // 读取动态映射表
        String re = null;
        if (null != this.runtimeEntryPageMap) {
            re = runtimeEntryPageMap.getString(host);
            // 有没有写默认规则呢？
            if (Strings.isBlank(re)) {
                re = runtimeEntryPageMap.getString("default");
            }
        }

        // 采用系统默认的 URL 映射
        if (Strings.isBlank(re)) {
            re = entryPageMap.getString(host);
            // 无论如何要有一个啊啊啊啊
            if (Strings.isBlank(re))
                re = entryPageMap.getString("default", "/u/h/login.html");
        }

        // 嗯嗯
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
            WnAuthSession se = auth().checkSession(ticket);

            if (null == se || se.isDead()) {
                return this._get_entry_page_url(host);
            }

            Wn.WC().setSession(se);
            // WnAccount me = se.getMe();

            // 如果当前用户的 ID 和名字相等，则必须强迫其改个名字
            // 这个就在界面里控制比较好
            // if (me.isNameSameAsId()) {
            // return "/u/h/rename.html";
            // }

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
