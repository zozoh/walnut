package com.site0.walnut.web2.controller;

import org.nutz.lang.util.NutMap;

import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.login.session.WnSession;
import com.site0.walnut.mvc.AbstractWnController;
import com.site0.walnut.mvc.WnMvcContext;
import com.site0.walnut.mvc.WnMvcRequest;
import com.site0.walnut.mvc.WnMvcView;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.WnWeb;
import com.site0.walnut.web2.view.WnDownloadView;

/**
 * <code>/o/content/*</code>：对照旧 <code>ObjModule.content(562)</code>（RM-NUTZ-08
 * 审核表 #8）。旧 <code>@AdaptBy(JsonAdaptor)</code> 下 Nutz NameInjector 有
 * 「缺键回退 query/form」的实际行为，旧版真实用法即
 * <code>GET /o/content/{downloadName}?str=xxx</code>（无 body 下载）。
 * <b>2026-11-10 用户修订裁定：content 是下载入口，<code>str</code>/<code>d</code>
 * 一律从 query string 取，不再解析请求体</b>（覆盖同日早前「严格只从 body JSON」的
 * 裁定；update 不受影响，仍严格只从 body 取）。路径 <code>**</code> 绑定 =
 * downloadName（0 段时为 null）。下载视图复用 RM-NUTZ-07 的 {@link WnDownloadView}
 * （6 参构造，UA 下载模式/ETag/304/Range 行为照旧）。过滤器
 * <code>SessionCheckFilter()</code>。
 */
public class ObjContentController extends AbstractWnController {

    @Override
    public WnMvcView handle(WnMvcContext ctx) {
        WnMvcRequest req = ctx.request();

        try {
            String downloadName = arg(ctx, 0);
            // str / d 从 query string 取（2026-11-10 用户修订：content 是下载入口，
            // 参数走 URL，不再解析请求体）
            NutMap qs = req.queryStringMap();
            String str = qs.getString("str");
            String download = qs.getString("d");
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
