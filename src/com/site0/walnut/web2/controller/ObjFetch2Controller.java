package com.site0.walnut.web2.controller;

import com.site0.walnut.mvc.AbstractWnController;
import com.site0.walnut.mvc.WnMvcContext;
import com.site0.walnut.mvc.WnMvcRequest;
import com.site0.walnut.mvc.WnMvcView;

/**
 * <code>/o/fetch2</code>：对照旧 <code>ObjModule.fetch2(124)</code>（RM-NUTZ-08 审核表 #2）。
 * 旧方法体即 <code>return fetch(str, true, false, req, resp)</code>，这里委托
 * {@link ObjFetchController#doFetch}。过滤器 <code>SessionCheckFilter(true)</code>。
 */
public class ObjFetch2Controller extends AbstractWnController {

    @Override
    public WnMvcView handle(WnMvcContext ctx) {
        WnMvcRequest req = ctx.request();
        // 这个接口开放给外部 app 调用
        cors(ctx);
        // 对于 options 放过
        if (options(ctx)) {
            return null;
        }
        try {
            // 2026-11-10 用户裁定：GET-only，参数只从 query string 取（不读 body form）
            String str = req.queryStringMap().getString("str");
            return ObjFetchController.doFetch(ctx, str, true, false);
        }
        catch (RuntimeException e) {
            return businessFailure(e);
        }
    }

}
