package com.site0.walnut.web2.controller;

import com.site0.walnut.mvc.WnMvcContext;
import com.site0.walnut.mvc.WnMvcView;

public class AppRunController extends AbstractAppController {
    @Override
    public WnMvcView handle(WnMvcContext ctx) {
        try {
            return run(ctx, params(ctx.request()));
        }
        catch (RuntimeException e) {
            return businessFailure(e);
        }
    }
}
