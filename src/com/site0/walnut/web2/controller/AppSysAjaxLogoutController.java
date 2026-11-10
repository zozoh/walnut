package com.site0.walnut.web2.controller;

import org.nutz.lang.util.NutMap;
import com.site0.walnut.login.session.WnSession;
import com.site0.walnut.mvc.WnMvcContext;
import com.site0.walnut.mvc.WnMvcView;
import com.site0.walnut.util.Wn;

public class AppSysAjaxLogoutController extends AbstractAppController {
    @Override
    public WnMvcView handle(WnMvcContext ctx) {
        cors(ctx);
        if (options(ctx))
            return WnMvcView.none();
        try {
            NutMap result = new NutMap();
            if (Wn.WC().hasTicket()) {
                String ticket = Wn.WC().getTicket();
                result.put("ticket", ticket);
                WnSession parent = auth(ctx).logout(ticket);
                if (null != parent && !parent.isExpired())
                    result.put("parent", parent.toBean(auth(ctx)));
            }
            return WnMvcView.ajax(result);
        }
        catch (RuntimeException e) {
            return businessFailure(e);
        }
    }
}
