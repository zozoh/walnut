package com.site0.walnut.web2.filter.router;

import org.nutz.lang.util.NutMap;

import com.site0.walnut.api.io.MimeMap;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.mvc.WnMvcContext;
import com.site0.walnut.mvc.WnMvcView;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;
import com.site0.walnut.web2.support.WnStaticResolver;

/**
 * C 记录静态站点路由（router 包；对照旧 {@code WnMainModule.handleRecordC}）。
 * <p>
 * 从请求属性 <code>wn_www_grp</code>/<code>wn_www_site</code> 找到
 * <code>/home/{domain}</code> 下的站点目录，按 index.html 回退/后缀白名单/
 * assets|resources 直通规则取文件，返回原始字节视图；文件不存在返回 <code>null</code>。
 */
public class StaticRouter {

    private final WnStaticResolver resolver;

    private final MimeMap mimes;

    public StaticRouter(WnStaticResolver resolver, MimeMap mimes) {
        this.resolver = resolver;
        this.mimes = mimes;
    }

    /**
     * @return 命中文件的视图；未命中返回 <code>null</code>
     */
    public WnMvcView handleRecordC(WnMvcContext ctx, String reqPath) {
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
