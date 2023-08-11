package org.nutz.walnut.web;

import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.nutz.json.Json;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.core.bean.WnIoObj;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.tmpl.WnTmpl;
import org.nutz.web.WebConfig;

public class WnConfig extends WebConfig {

    public WnConfig(String path) {
        super(Streams.fileInr(path));
        putAll(System.getProperties());
        putAll(System.getenv());
    }

    // Chrome 51 开始，浏览器的 Cookie 新增加了一个SameSite属性，
    // 用来防止 CSRF 攻击和用户追踪。
    // 为此默认的，我们需要为 Cookie 模板增加一个 SameSite 属性
    // Set-Cookie: CookieName=CookieValue; SameSite=None; Secure
    // - Strict: 完全不发
    // - Lax： 大多数情况不发
    // - None: 总是发，需要配合 Secure 属性
    // @see https://www.ruanyifeng.com/blog/2019/09/cookie-samesite.html
    public WnTmpl getCookieTmpl(boolean asHttps) {
        String str = String.format("%s=${ticket}", Wn.AT_SEID);
        String sameSite = this.get("cookie_same_site", "Auto").toLowerCase();
        // 总是发，这样再前端套 https 代理时，也会强制发
        if ("none".equals(sameSite)) {
            str += "; SameSite=None; Secure";
        }
        // 如果 https 就总是发
        if ("auto".equals(sameSite)) {
            if (asHttps) {
                str += "; SameSite=None; Secure";
            }
        }
        // 强制
        else if ("strict".equals(sameSite)) {
            str += "; SameSite=Strict";
        }
        return WnTmpl.parse(str);
    }

    public WnTmpl getCookieTmpl(HttpServletRequest req) {
        String protocol = req.getProtocol().toLowerCase();
        boolean asHttps = "https".equals(protocol);
        return getCookieTmpl(asHttps);
    }

    public String[] getWebIocPkgs() {
        String str = this.get("web-ioc-pkgs");
        if (Strings.isBlank(str))
            return new String[0];
        return Strings.splitIgnoreBlank(str, "\n");
    }

    public String[] getWebModulePkgs() {
        String str = this.get("web-module-pkgs");
        if (Strings.isBlank(str))
            return new String[0];
        return Strings.splitIgnoreBlank(str, "\n");
    }

    public String[] getInitSetup() {
        String str = this.get("init-setup");
        if (Strings.isBlank(str))
            return new String[0];
        return Strings.splitIgnoreBlank(str, "\n");
    }

    public String[] getJvmboxPkgs() {
        String str = this.get("jvmbox-pkgs");
        if (Strings.isBlank(str))
            return new String[0];
        return Strings.splitIgnoreBlank(str, "\n");
    }

    public List<WnInitMount> getInitMount() {
        List<WnInitMount> list = new LinkedList<WnInitMount>();
        String str = this.get("init-mnt");
        if (!Strings.isBlank(str)) {
            String[] lines = Strings.splitIgnoreBlank(str, "\n");
            for (String line : lines) {
                list.add(new WnInitMount(line));
            }
        }
        return list;
    }

    public NutMap getInitUsrEnvs() {
        String str = this.get("init-usr-envs");
        if (Strings.isBlank(str))
            return new NutMap();
        return Json.fromJson(NutMap.class, str);
    }

    public WnObj getRootTreeNode() {
        String id = this.get("root-id");

        WnObj o = new WnIoObj();
        o.id(id);
        o.path("/");
        o.race(WnRace.DIR);
        o.name("");
        o.lastModified(Wn.now());
        o.createTime(Wn.now());
        o.creator("root").mender("root").group("root");
        o.mode(0755);

        return o;
    }

    // public NutMap getEntryPages() {
    // String json = this.get("entry-pages", "{}");
    // return Json.fromJson(NutMap.class, json);
    // }

    public String getSysEntryUrl() {
        return this.get("sys-entry-url", "/a/login/");
    }

}
