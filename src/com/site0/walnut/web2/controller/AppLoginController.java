package com.site0.walnut.web2.controller;

import com.site0.walnut.mvc.WnMvcContext;
import com.site0.walnut.mvc.WnMvcView;

public class AppLoginController extends AbstractAppController {
    @Override
    public WnMvcView handle(WnMvcContext ctx) {
        if (ctx.request().getPath().endsWith("/"))
            return new AppLoginPageController().handle(ctx);
        return WnMvcView.redirect("/a/login/");
    }
}
