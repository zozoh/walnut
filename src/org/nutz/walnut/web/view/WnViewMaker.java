package org.nutz.walnut.web.view;

import org.nutz.ioc.Ioc;
import org.nutz.mvc.View;
import org.nutz.mvc.ViewMaker;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.tmpl.WnTmpl;
import org.nutz.walnut.web.WnConfig;
import org.nutz.web.ajax.AjaxView;

public class WnViewMaker implements ViewMaker {

    @Override
    public View make(Ioc ioc, String type, String value) {
        // Chrome 51 开始，浏览器的 Cookie 新增加了一个SameSite属性，
        // 用来防止 CSRF 攻击和用户追踪。
        // 为此默认的，我们需要为 Cookie 模板增加一个 SameSite 属性
        // Set-Cookie: CookieName=CookieValue; SameSite=None; Secure
        // - Strict: 完全不发
        // - Lax： 大多数情况不发
        // - None: 总是发，需要配合 Secure 属性
        // @see https://www.ruanyifeng.com/blog/2019/09/cookie-samesite.html
        WnConfig conf = ioc.get(WnConfig.class, "conf");
        String dft_cookie = conf.get("dft_cookie",
                                     String.format("%s=${ticket}; SameSite=None; Secure",
                                                   Wn.AT_SEID));

        String[] ss = Ws.splitIgnoreBlank(value);
        String cookie_t = dft_cookie;
        String path = value;
        if (null != ss && ss.length > 1) {
            cookie_t = Ws.sBlank(dft_cookie, ss[0]);
            path = ss[1];

        }
        WnTmpl cookie = WnTmpl.parse(cookie_t);

        // 设置 cookie 并重定向
        if ("++cookie>>".equals(type)) {
            return new WnAddCookieViewWrapper(cookie, path);
        }
        // 从 cookie 移除并重定向
        else if ("--cookie>>".equals(type)) {
            return new WnDelCookieViewWrapper(value);
        }
        // 设置 cookie 并输出 AJAX 返回
        else if ("++cookie->ajax".equals(type)) {
            return new WnAddCookieViewWrapper(cookie, new AjaxView(), path);
        }

        // 呃，不认识了 ...
        return null;
    }

}
