package com.site0.walnut.web2.controller;

import java.util.LinkedList;
import java.util.List;

import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.login.session.WnSession;
import com.site0.walnut.mvc.AbstractWnController;
import com.site0.walnut.mvc.WnMvcContext;
import com.site0.walnut.mvc.WnMvcRequest;
import com.site0.walnut.mvc.WnMvcView;
import com.site0.walnut.util.Wn;

/**
 * <code>/o/ancestors</code>：对照旧 <code>ObjModule.ancestors(142)</code>（JETTY-08
 * 审核表 #3）。逐行照抄旧方法体，只换绑定/视图/响应写法。过滤器
 * <code>SessionCheckFilter()</code>。
 */
public class ObjAncestorsController extends AbstractWnController {

    @Override
    public WnMvcView handle(WnMvcContext ctx) {
        WnMvcRequest req = ctx.request();
        try {
            String str = params(req).getString("str");
            WnSession se = Wn.WC().checkSession();
            WnObj meta = Wn.checkObj(io(ctx), se, str);
            LinkedList<WnObj> ans = new LinkedList<>();
            meta.loadParents(ans, false);
            List<WnObj> list = ans;
            return WnMvcView.ajax(list);
        }
        catch (RuntimeException e) {
            return businessFailure(e);
        }
    }

}
