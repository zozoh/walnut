package com.site0.walnut.web2.controller;

import java.net.URLDecoder;
import java.util.LinkedList;

import org.nutz.lang.Encoding;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.login.session.WnSession;
import com.site0.walnut.mvc.AbstractWnController;
import com.site0.walnut.mvc.WnMvcContext;
import com.site0.walnut.mvc.WnMvcRequest;
import com.site0.walnut.mvc.WnMvcView;
import com.site0.walnut.util.Wn;

/**
 * <code>/o/fetch</code>：对照旧 <code>ObjModule.fetch(91)</code>（JETTY-08 审核表 #1）。
 * 逐行照抄旧方法体，只换绑定/视图/响应写法；{@link ObjFetch2Controller} 复用
 * {@link #doFetch}（旧 <code>fetch2</code> 就是委托 <code>fetch(str, true, false)</code>）。
 * 过滤器 <code>SessionCheckFilter(true)</code>（未通过 401）。
 */
public class ObjFetchController extends AbstractWnController {

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
            NutMap params = params(req);
            String str = params.getString("str");
            boolean loadPath = params.getBoolean("path", false);
            boolean loadAxis = params.getBoolean("axis", false);
            return doFetch(ctx, str, loadPath, loadAxis);
        }
        catch (RuntimeException e) {
            return businessFailure(e);
        }
    }

    static WnMvcView doFetch(WnMvcContext ctx, String str, boolean loadPath, boolean loadAxis) {
        WnSession se = Wn.WC().checkSession();
        String ph = URLDecoder.decode(str, Encoding.CHARSET_UTF8);
        WnObj o = Wn.checkObj(ctx.io(), se, ph);

        if (loadAxis) {
            LinkedList<WnObj> ans = new LinkedList<>();
            o.loadParents(ans, false);
            o.put("ancestors", ans);
        }

        if (loadPath) {
            o.path();
        }

        return WnMvcView.ajax(o);
    }

}
