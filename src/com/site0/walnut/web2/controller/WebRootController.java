package com.site0.walnut.web2.controller;


import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.nutz.lang.util.NutMap;

import com.site0.walnut.WnVersion;
import com.site0.walnut.mvc.AbstractWnController;
import com.site0.walnut.web.WnConfig;
import com.site0.walnut.mvc.WnMvcContext;
import com.site0.walnut.mvc.WnMvcFilterException;
import com.site0.walnut.mvc.WnMvcView;
import com.site0.walnut.util.Wn;


import com.site0.walnut.mvc.WnDefaultStaticService;
import com.site0.walnut.mvc.WnIoStaticResolver;

/**
 * 统一 {@code /**} 兜底控制器：内部处理 {@code /}、{@code /favicon}、
 * {@code /u/version}、{@code /u/runtime} 与静态资源。
 * <p>
 * 静态/C 记录命中由 {@link WnDefaultStaticService} 直接写响应；
 * 未命中抛 {@link WnMvcFilterException}(404)，由 {@code WnMvcChain.fail} 按页面/API
 * 渲染错误体（页面 HTML / API JSON）。
 */
public class WebRootController extends AbstractWnController {

    private static final byte[] FAVICON = __read_favicon();

    @Override
    public WnMvcView handle(WnMvcContext ctx) {
        String path = ctx.request().getPath();
        if ("/".equals(path))
            return __root(ctx);
        if ("/favicon".equals(path))
            return __favicon(ctx);
        if ("/u/version".equals(path))
            return __version(ctx);
        if ("/u/runtime".equals(path))
            return __runtime(ctx);
        return __any(ctx, path);
    }

    // ----------------------------------------------------------------------
    // 各路径
    // ----------------------------------------------------------------------

    private WnMvcView __root(WnMvcContext ctx) {
        // C 记录：按域静态站点的 index.html
        if ("yes".equals(ctx.request().getAttr("wn_www_static")))
            return __any(ctx, "");

        // 有会话 → 默认应用
        if (null != ctx.session() && !ctx.session().isExpired()) {
            String appName = ctx.session().getEnvString("OPEN", "wn.console");
            return WnMvcView.redirect("/a/open/" + appName);
        }
        // 无会话 → 系统入口
        return WnMvcView.redirect(__entry_url(ctx));
    }

    private String __entry_url(WnMvcContext ctx) {
        WnConfig conf = null == ctx.services() ? null : ctx.services().getConfig();
        return null == conf ? "/a/login/" : conf.getSysEntryUrl();
    }

    private WnMvcView __favicon(WnMvcContext ctx) {
        if (null == FAVICON)
            throw new WnMvcFilterException(404, "e.mvc.nofound", "favicon not found");
        return WnMvcView.raw(FAVICON, "image/x-icon");
    }

    private WnMvcView __version(WnMvcContext ctx) {
        return WnMvcView.raw(WnVersion.getName().getBytes(StandardCharsets.UTF_8),
                             "text/plain;charset=utf-8");
    }

    private WnMvcView __runtime(WnMvcContext ctx) {
        Object rt = Wn.getRuntime();
        return WnMvcView.json(null == rt ? new NutMap() : rt);
    }

    private WnMvcView __any(WnMvcContext ctx, String path) {
        // 静态服务：C 记录静态站点（原 webRoot 真实文件来源已随 WebContent 弃用移除）
        WnDefaultStaticService staticService = new WnDefaultStaticService(new WnIoStaticResolver(ctx.io()),
                                                                          ctx.mimes());
        if (staticService.serve(ctx))
            return null;

        // 未命中：抛 404，由 Chain.fail 按页面/API 渲染
        throw new WnMvcFilterException(404, "e.mvc.nofound", "no route: " + path);
    }

    // ----------------------------------------------------------------------

    private static byte[] __read_favicon() {
        try (InputStream in = WebRootController.class.getResourceAsStream("/com/site0/walnut/web/favicon.ico")) {
            return null == in ? null : in.readAllBytes();
        }
        catch (Throwable e) {
            return null;
        }
    }

}
