package org.nutz.walnut.web.view;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.mvc.View;
import org.nutz.walnut.util.Wn;

public class WnDelCookieViewWrapper implements View {

    private static final String fmt = "%s=deleted;Path=/;expires=Thu, Jan 01 1970 00:00:00 UTC;";

    private View view;

    public WnDelCookieViewWrapper(View view) {
        this.view = view;
    }

    @Override
    public void render(HttpServletRequest req,
                       HttpServletResponse resp,
                       Object obj) throws Throwable {

        resp.setHeader("SET-COOKIE", String.format(fmt, Wn.AT_SEID));

        // 输出对象
        if (null != view)
            view.render(req, resp, obj);
    }

}
