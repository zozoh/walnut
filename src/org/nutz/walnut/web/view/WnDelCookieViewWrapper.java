package org.nutz.walnut.web.view;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.lang.Strings;
import org.nutz.mvc.View;
import org.nutz.mvc.view.ServerRedirectView;
import org.nutz.walnut.util.Wn;

public class WnDelCookieViewWrapper implements View {

    private static final String fmt = "%s=deleted;Path=/;expires=Thu, Jan 01 1970 00:00:00 UTC;";

    private String cookieName;

    private View view;
    
    public WnDelCookieViewWrapper(View view) {
        this.view = view;
        this.cookieName = Wn.AT_SEID;
    }

    public WnDelCookieViewWrapper(String value) {
        String[] ss = Strings.splitIgnoreBlank(value);
        // 只有一个: @Ok("--cookie>>:/")
        if (ss.length == 1) {
            this.cookieName = Wn.AT_SEID;
            this.view = new ServerRedirectView(ss[0]);
        }
        // 两个 @Ok("--cookie>>:DSEID,${obj}")
        else {
            this.cookieName = ss[0];
            this.view = new ServerRedirectView(ss[1]);
        }
    }

    @Override
    public void render(HttpServletRequest req, HttpServletResponse resp, Object obj)
            throws Throwable {

        resp.setHeader("SET-COOKIE", String.format(fmt, cookieName));

        // 输出对象
        if (null != view)
            view.render(req, resp, obj);
    }

}
