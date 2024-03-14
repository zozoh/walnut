package com.site0.walnut.web.view;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.lang.Lang;
import com.site0.walnut.util.tmpl.WnTmpl;
import com.site0.walnut.web.WnConfig;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.View;
import org.nutz.mvc.view.ServerRedirectView;
import com.site0.walnut.util.Ws;

public class WnAddCookieViewWrapper implements View {

    private WnConfig conf;

    private View view;

    public WnAddCookieViewWrapper(WnConfig conf, String value) {
        // 只有一个: @Ok("++cookie>>:/")
        // 两个: @Ok("++cookie>>:DSEID=${dseid},${obj.url}")
        this.conf = conf;
        String path = Ws.sBlank(value, "/");
        this.view = new ServerRedirectView(path);
    }

    public WnAddCookieViewWrapper(WnConfig conf, View view, String value) {
        this.conf = conf;
        this.view = view;
        if (null == view) {
            String path = Ws.sBlank(value, "/");
            this.view = new ServerRedirectView(path);
        }
    }

    @Override
    public void render(HttpServletRequest req, HttpServletResponse resp, Object obj)
            throws Throwable {

        // 自动获取模板
        WnTmpl cookie = conf.getCookieTmpl(req);

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
