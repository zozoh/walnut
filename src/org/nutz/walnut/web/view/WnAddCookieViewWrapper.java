package org.nutz.walnut.web.view;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.lang.Strings;
import org.nutz.mvc.View;
import org.nutz.walnut.util.Wn;

public class WnAddCookieViewWrapper implements View {

    private View view;

    public WnAddCookieViewWrapper(View view) {
        this.view = view;
    }

    @Override
    public void render(HttpServletRequest req,
                       HttpServletResponse resp,
                       Object obj) throws Throwable {

        String seid = Wn.WC().SEID();
        if (!Strings.isBlank(seid)) {
            resp.addHeader("SET-COOKIE", Wn.AT_SEID + "=" + seid + "; Path=/;");
        }

        // 输出对象
        if (null != view)
            view.render(req, resp, obj);
    }

}
