package com.site0.walnut.mvc;

import org.nutz.lang.util.NutMap;

import com.site0.walnut.api.io.MimeMap;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;

/**
 * {@link WnMvcStaticService} 的默认实现（RM-NUTZ-14 自 {@code web2.jetty.Jetty9StaticService}
 * 移入本包并改名——原实现纯 JDK，与 Jetty 无关）。
 * <p>
 * 只保留「C 记录静态站点」来源（对照旧 {@code WnMainModule.handleRecordC}）：
 * 从请求属性 <code>wn_www_grp</code>/<code>wn_www_site</code> 找到
 * <code>/home/{domain}</code> 下的站点目录，按 index.html 回退/后缀白名单/
 * assets|resources 直通规则取文件，返回原始字节视图。
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
        else if (path.matches("^.+[.](js(.map)?|css|json|html|txt|pdf|gif|png|svg|jpe?g)$")) {
            // 保持原路径
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
        byte[] data = resolver.readBytes(obj);
        if (null == data)
            return null;
        String contentType = obj.mime();
        if (Ws.isBlank(contentType) && null != mimes)
            contentType = mimes.getMime(obj.type());
        if (Ws.isBlank(contentType))
            contentType = "application/octet-stream";
        return WnMvcView.raw(data, contentType);
    }

}
