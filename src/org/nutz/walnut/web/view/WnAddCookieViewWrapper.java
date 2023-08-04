package org.nutz.walnut.web.view;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.lang.Lang;
import org.nutz.walnut.util.tmpl.WnTmpl;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.View;
import org.nutz.mvc.view.ServerRedirectView;
import org.nutz.walnut.util.Ws;

public class WnAddCookieViewWrapper implements View {

    private WnTmpl cookie;

    private View view;

    public WnAddCookieViewWrapper(WnTmpl cookie, String value) {
        // 只有一个: @Ok("++cookie>>:/")
        // 两个: @Ok("++cookie>>:DSEID=${dseid},${obj.url}")
        this.cookie = cookie;
        String path = Ws.sBlank(value, "/");
        this.view = new ServerRedirectView(path);
    }

    public WnAddCookieViewWrapper(WnTmpl cookie, View view, String value) {
        this.cookie = cookie;
        this.view = view;
        String path = Ws.sBlank(value, "/");
        this.view = new ServerRedirectView(path);
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
