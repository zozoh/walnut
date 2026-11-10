package com.site0.walnut.web2.controller;

import java.util.ArrayList;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.lookup.WnLookup;
import com.site0.walnut.lookup.impl.WnLookupMaker;
import com.site0.walnut.mvc.WnMvcContext;
import com.site0.walnut.mvc.WnMvcView;
import com.site0.walnut.util.Ws;

public class AppLookupController extends AbstractAppController {
    @Override
    public WnMvcView handle(WnMvcContext ctx) {
        try {
            // 2026-11-10 用户裁定：GET-only，参数只从 query string 取（不读 body form）
            NutMap params = ctx.request().queryStringMap();
            WnLookupMaker maker = ctx.services().getLookupMaker();
            if (bool(params, "reset"))
                maker.clear();
            WnLookup lookup = maker.getLookup(arg(ctx, 0));
            String id = params.getString("id");
            String hint = params.getString("hint");
            if (!Ws.isBlank(id))
                return WnMvcView.ajax(lookup.fetch(id));
            if (!Ws.isBlank(hint))
                return WnMvcView.ajax(lookup.lookup(hint, params.getInt("limit", 30)));
            return WnMvcView.ajax(new ArrayList<>());
        }
        catch (RuntimeException e) {
            return businessFailure(e);
        }
    }
}
