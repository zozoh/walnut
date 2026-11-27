package com.site0.walnut.mvc;

import org.nutz.lang.util.NutMap;

import com.site0.walnut.api.io.MimeMap;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.util.Wfiles;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.range.WnGetInputStream;

/**
 * {@link WnMvcStaticService} 的默认实现（RM-NUTZ-14 自 {@code web2.jetty.Jetty9StaticService}
 * 移入本包并改名——原实现纯 JDK，与 Jetty 无关）。
 * <p>
 * 只保留「C 记录静态站点」来源（对照旧 {@code WnMainModule.handleRecordC}）：
 * 从请求属性 <code>wn_www_grp</code>/<code>wn_www_site</code> 找到
 * <code>/home/{domain}</code> 下的站点目录，按 index.html 回退/后缀白名单/
 * assets|resources 直通规则取文件，返回 {@link WnMvcView#asset} 视图
 * （ETag / 304 / Range / HEAD 由视图负责，文件内容流式输出）。
 * <p>
 * 旧实现的「app-root 真实文件」来源已随 WebContent 弃用移除（2026-11-11 用户定稿：
 * Walnut 不再有 webRoot）。
 */
public class WnDefaultStaticService implements WnMvcStaticService {

    private final WnStaticResolver resolver;

    private final MimeMap mimes;

    public WnDefaultStaticService(WnStaticResolver resolver, MimeMap mimes) {
        this.resolver = resolver;
        this.mimes = mimes;
    }

    @Override
    public boolean serve(WnMvcContext ctx) {
        String path = ctx.request().getPath();

        // C 记录静态站点
        WnMvcView view = __handle_record_c(ctx, path);
        if (null != view) {
            view.render(ctx);
            return true;
        }
        // 未命中：由 dispatcher 落 404
        return false;
    }

    /**
     * C 记录静态站点：命中返回视图；未命中返回 <code>null</code>。
     */
    protected WnMvcView __handle_record_c(WnMvcContext ctx, String reqPath) {
        Object grp = ctx.request().getAttr("wn_www_grp");
        Object site = ctx.request().getAttr("wn_www_site");
        if (null == grp || null == site)
            return null;

        String homePath = "/home/" + grp;
        NutMap vars = Wlang.map("HOME", homePath);
        vars.put("PWD", homePath);
        String dirPath;
        try {
            dirPath = Wn.normalizeFullPath(site.toString(), vars);
        }
        catch (Throwable e) {
            return null;
        }
        WnObj oDir = null == resolver ? null : resolver.check(dirPath);
        if (null == oDir)
            return null;

        String path = null == reqPath ? "" : reqPath;
        while (path.startsWith("/"))
            path = path.substring(1);
        if (Ws.isBlank(path)) {
            path = "index.html";
        }
        else if (__is_asset_path(path)) {
            // 后缀在 mime 表里（或 assets|resources 前缀）：保持原路径
        }
        else if (path.matches("^(assets|resources)[/].+$")) {
            // 保持原路径
        }
        else {
            path = "index.html";
        }

        WnObj obj = resolver.fetch(oDir, path);
        if (null == obj)
            return null;
        String contentType = obj.mime();
        if (Ws.isBlank(contentType) && null != mimes)
            contentType = mimes.getMime(obj.type());
        if (Ws.isBlank(contentType))
            contentType = "application/octet-stream";
        WnGetInputStream src = resolver.open(obj);
        if (null == src)
            return null;
        return WnMvcView.asset(src, contentType);
    }

    /**
     * 路径后缀是不是「静态资源」：以 {@link MimeMap#keys()}（mime.properties 的后缀集合）
     * 为准，外加 <code>map</code>（sourcemap 不是 mime）。
     *
     * <p>这样 <code>mp4</code> / <code>webm</code> / <code>woff2</code> 之类不用再往
     * 正则里一个个补；<code>mimes</code> 缺失时退回旧白名单，保持兼容。</p>
     */
    protected boolean __is_asset_path(String path) {
        String type = Wfiles.getSuffixName(path);
        if (null != type && !type.isEmpty()) {
            String t = type.toLowerCase();
            if ("map".equals(t))
                return true;
            if (null != mimes && mimes.keys().contains(t))
                return true;
        }
        return path.matches("^.+[.](js(.map)?|css|json|html|txt|pdf|gif|png|svg|jpe?g)$");
    }

}
