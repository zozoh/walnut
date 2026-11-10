package com.site0.walnut.web2.controller;

import org.nutz.lang.util.NutMap;
import com.site0.walnut.mvc.WnMvcContext;
import com.site0.walnut.mvc.WnMvcView;

public class AppLoginModeController extends AbstractAppController {
    @Override
    public WnMvcView handle(WnMvcContext ctx) {
        try {
            NutMap result = new NutMap();
            result.put("system", conf(ctx).getSysLoginMode());
            result.put("domain", conf(ctx).getDomainLoginMode());
            return WnMvcView.ajax(result);
        }
        catch (RuntimeException e) {
            return businessFailure(e);
        }
    }
}
