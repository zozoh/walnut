package com.site0.walnut.web2.view;

import com.site0.walnut.util.Ws;
import com.site0.walnut.mvc.WnMvcContext;
import com.site0.walnut.mvc.WnMvcView;
import com.site0.walnut.util.Wn;

public class WnDelCookieView implements WnMvcView {

    private final String cookieName;
    private final WnMvcView view;

    public WnDelCookieView(WnMvcView view) {
        this.cookieName = Wn.AT_SEID;
        this.view = view;
    }

    public WnDelCookieView(String value) {
        String[] parts = Ws.splitIgnoreBlank(value);
        this.cookieName = parts.length == 1 ? Wn.AT_SEID : parts[0];
        this.view = WnMvcView.redirect(parts.length == 1 ? parts[0] : parts[1]);
    }

    @Override
    public void render(WnMvcContext ctx) {
        ctx.response().removeCookie(cookieName);
        if (null != view)
            view.render(ctx);
    }
}
