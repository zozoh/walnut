package org.nutz.walnut.web.view;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.tmpl.Tmpl;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.View;
import org.nutz.mvc.view.ServerRedirectView;
import org.nutz.walnut.util.Wn;

public class WnAddCookieViewWrapper implements View {

    private Tmpl cookie;

    private View view;

    public WnAddCookieViewWrapper(String value) {
        String[] ss = Strings.splitIgnoreBlank(value);
        // 只有一个: @Ok("++cookie>>:/")
        if (ss.length == 1) {
            this.cookie = Tmpl.parsef("%s=${ticket}", Wn.AT_SEID);
            this.view = new ServerRedirectView(ss[0]);
        }
        // 两个: @Ok("++cookie>>:DSEID=${dseid},${obj.url}")
        else {
            this.cookie = Tmpl.parse(ss[0]);
            this.view = new ServerRedirectView(ss[1]);
        }
    }

    public WnAddCookieViewWrapper(View view) {
        this.cookie = Tmpl.parsef("%s=${ticket}", Wn.AT_SEID);
        this.view = view;
    }

    public WnAddCookieViewWrapper(View view, String value) {
        // 默认 cookie 模板
        if (Strings.isBlank(value)) {
            this.cookie = Tmpl.parsef("%s=${ticket}", Wn.AT_SEID);
            this.view = view;
        }
        // 指定了 cookie 模板
        else {
            this.cookie = Tmpl.parsef(value);
            this.view = view;
        }
    }

    @Override
    public void render(HttpServletRequest req, HttpServletResponse resp, Object obj)
            throws Throwable {

        if (null != obj && !(obj instanceof Throwable)) {
            NutMap context = Lang.obj2map(obj, NutMap.class);
            String cookieStr = cookie.render(context, false);
            resp.addHeader("SET-COOKIE", cookieStr + "; Path=/;");
        }

        // 输出对象
        if (null != view)
            view.render(req, resp, obj);
    }

}
