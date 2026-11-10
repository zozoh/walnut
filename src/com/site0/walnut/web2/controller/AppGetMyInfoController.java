package com.site0.walnut.web2.controller;

import com.site0.walnut.login.session.WnSession;
import com.site0.walnut.mvc.WnMvcContext;
import com.site0.walnut.mvc.WnMvcView;
import com.site0.walnut.util.Wn;

public class AppGetMyInfoController extends AbstractAppController {
    @Override
    public WnMvcView handle(WnMvcContext ctx) {
        cors(ctx);
        if (options(ctx))
            return WnMvcView.none();
        try {
            WnSession se = Wn.WC().checkSession(auth(ctx));
            return WnMvcView.ajax(se.toBean(auth(ctx)));
        }
        catch (RuntimeException e) {
            return businessFailure(e);
        }
    }
}
