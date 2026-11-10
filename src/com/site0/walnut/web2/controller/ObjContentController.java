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
import com.site0.walnut.web.util.WnWeb;
import com.site0.walnut.web2.view.WnDownloadView;

/**
 * <code>/o/content/*</code>：对照旧 <code>ObjModule.content(562)</code>（JETTY-08
 * 审核表 #8）。旧 <code>@AdaptBy(JsonAdaptor)</code>：整个请求体就是 JSON，query
 * string 完全忽略（2026-11-10 用户定稿：<code>str</code>/<code>d</code> 只从 body
 * 取，不做旧 Nutz NameInjector 的缺键回退）；路径 <code>?</code> 绑定 =
 * downloadName。下载视图复用 JETTY-07 的 {@link WnDownloadView}（6 参构造，UA
 * 下载模式/ETag/304/Range 行为照旧）。过滤器 <code>SessionCheckFilter()</code>。
 */
public class ObjContentController extends AbstractWnController {

    @Override
    public WnMvcView handle(WnMvcContext ctx) {
        WnMvcRequest req = ctx.request();
        // 这个接口开放给外部 app 调用
        cors(ctx);

        // Options 无视
        if (options(ctx)) {
            return null;
        }
        try {
            String downloadName = arg(ctx, 0);
            java.io.InputStream ins = req.getInputStream();
            NutMap body = null == ins ? null : Json.fromJson(NutMap.class, Streams.utf8r(ins));
            String str = null == body ? null : body.getString("str");
            String download = null == body ? null : body.getString("d");
            String ua = req.getHeader("User-Agent");
            String etag = req.getHeader("If-None-Match");
            String range = req.getHeader("Range");

            // 获取当前会话
            WnSession se = Wn.WC().checkSession();

            // 取得对应对象
            WnObj o = Wn.checkObj(io(ctx), se, str);

            // 确保可读，同时处理链接文件
            o = Wn.WC().whenRead(o, false);

            // 纠正一下下载模式
            ua = WnWeb.autoUserAgent(o, ua, download);

            // 返回下载视图
            return new WnDownloadView(io(ctx), o, downloadName, ua, etag, range);
        }
        catch (RuntimeException e) {
            return businessFailure(e);
        }
    }

}
