package com.site0.walnut.web2.jetty;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import com.site0.walnut.mvc.WnMvcContext;
import com.site0.walnut.mvc.WnMvcStaticService;
import com.site0.walnut.mvc.WnMvcView;
import com.site0.walnut.web2.filter.router.StaticRouter;

/**
 * {@link WnMvcStaticService} 的 Jetty9 实现（JETTY-05）。
 * <p>
 * 两条来源：
 * <ol>
 * <li><b>C 记录静态站点</b>：请求带 <code>wn_www_static=yes</code> 时委托
 * {@link StaticRouter} 从 <code>/home/{domain}</code> 读 IO 对象。</li>
 * <li><b>WebContent</b>：工作目录/配置的 <code>app-root</code> 下的真实文件。</li>
 * </ol>
 * 文件不存在返回 <code>false</code>，由 {@code WnMvcDispatcher.__fallback} 落 404 HTML。
 */
public class Jetty9StaticService implements WnMvcStaticService {

    private final StaticRouter router;

    private final File webRoot;

    public Jetty9StaticService(StaticRouter router, File webRoot) {
        this.router = router;
        this.webRoot = webRoot;
    }

    @Override
    public boolean serve(WnMvcContext ctx) {
        String path = ctx.request().getPath();

        // 1) C 记录静态站点
        WnMvcView view = router.handleRecordC(ctx, path);
        if (null != view) {
            view.render(ctx);
            return true;
        }

        // 2) WebContent 真实文件
        File f = __resolve(path);
        if (null == f || !f.isFile())
            return false;
        try {
            byte[] data = Files.readAllBytes(f.toPath());
            String contentType = __mime(ctx, f.getName());
            WnMvcView.raw(data, contentType).render(ctx);
            return true;
        }
        catch (IOException e) {
            return false;
        }
    }

    private File __resolve(String path) {
        if (null == webRoot || null == path)
            return null;
        String rel = path;
        while (rel.startsWith("/"))
            rel = rel.substring(1);
        if (rel.isEmpty() || rel.contains(".."))
            return null;
        File f = new File(webRoot, rel);
        try {
            String rootPath = webRoot.getCanonicalPath();
            String fPath = f.getCanonicalPath();
            if (!fPath.equals(rootPath) && !fPath.startsWith(rootPath + File.separator))
                return null;
            return f;
        }
        catch (IOException e) {
            return null;
        }
    }

    private String __mime(WnMvcContext ctx, String fileName) {
        int i = fileName.lastIndexOf('.');
        String ext = i < 0 ? "" : fileName.substring(i + 1).toLowerCase();
        String mime = null;
        if (null != ctx.mimes())
            mime = ctx.mimes().getMime(ext, null);
        return null == mime || mime.isEmpty() ? "application/octet-stream" : mime;
    }

    // 便于测试/装配读取
    public File webRoot() {
        return webRoot;
    }

    public StaticRouter router() {
        return router;
    }

}
