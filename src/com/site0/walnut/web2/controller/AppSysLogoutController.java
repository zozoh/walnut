package com.site0.walnut.web2.controller;

import com.site0.walnut.login.session.WnSession;
import com.site0.walnut.mvc.WnMvcContext;
import com.site0.walnut.mvc.WnMvcView;
import com.site0.walnut.util.Wn;
import com.site0.walnut.web2.view.WnAddCookieView;
import com.site0.walnut.web2.view.WnDelCookieView;

public class AppSysLogoutController extends AbstractAppController {
    @Override
    public WnMvcView handle(WnMvcContext ctx) {
        boolean ajax = bool(params(ctx.request()), "ajax");
        WnMvcView view = ajax ? WnMvcView.ajax(null) : WnMvcView.redirect(conf(ctx).getSysEntryUrl());
        if (Wn.WC().hasTicket()) {
            WnSession parent = auth(ctx).logout(Wn.WC().getTicket());
            if (null != parent && !parent.isExpired()) {
                Object data = parent.toBean(auth(ctx));
                return new WnAddCookieView(conf(ctx), ajax ? WnMvcView.ajax(data) : view, data);
            }
        }
        return new WnDelCookieView(view);
    }
}
