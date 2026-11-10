package com.site0.walnut.web2.controller;

import org.nutz.json.Json;
import org.nutz.lang.Streams;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.login.session.WnSession;
import com.site0.walnut.mvc.AbstractWnController;
import com.site0.walnut.mvc.WnMvcContext;
import com.site0.walnut.mvc.WnMvcRequest;
import com.site0.walnut.mvc.WnMvcView;
import com.site0.walnut.util.Wn;

/**
 * <code>/o/update</code>：对照旧 <code>ObjModule.update(190)</code>（JETTY-08 审核表 #4）。
 * 旧 <code>@AdaptBy(JsonAdaptor)</code>：整个请求体就是 JSON，query string 完全忽略
 * （2026-11-10 用户定稿：命名参数 <code>str</code> 也只从 body 取，不做旧 Nutz
 * NameInjector 的缺键回退）；未注解的 <code>NutMap map</code> = 整个 body map（含
 * <code>str</code> 键）。过滤器 <code>SessionCheckFilter()</code>。
 */
public class ObjUpdateController extends AbstractWnController {

    @Override
    public WnMvcView handle(WnMvcContext ctx) {
        WnMvcRequest req = ctx.request();
        try {
            java.io.InputStream ins = req.getInputStream();
            NutMap map = null == ins ? null : Json.fromJson(NutMap.class, Streams.utf8r(ins));
            String str = null == map ? null : map.getString("str");

            // 确保 str 的形式正确
            WnSession se = Wn.WC().checkSession();

            // 取得并写入
            WnObj o = Wn.checkObj(io(ctx), se, str);
            io(ctx).appendMeta(o, map);
            return WnMvcView.ajax(o);
        }
        catch (RuntimeException e) {
            return businessFailure(e);
        }
    }

}
