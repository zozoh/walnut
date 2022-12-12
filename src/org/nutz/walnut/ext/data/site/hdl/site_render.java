package org.nutz.walnut.ext.data.site.hdl;

import org.nutz.walnut.ext.data.site.SiteContext;
import org.nutz.walnut.ext.data.site.SiteFilter;
import org.nutz.walnut.ext.data.site.render.SitePageRenderConfig;
import org.nutz.walnut.ext.data.site.render.SiteRendering;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class site_render extends SiteFilter {

    @Override
    protected void process(WnSystem sys, SiteContext fc, ZParams params) {
        // 分析参数
        String targetPath = params.val(0);
        String[] langs = params.getAs("lang", String[].class);
        SitePageRenderConfig conf = params.getAs("conf", SitePageRenderConfig.class);

        // 准备服务类
        SiteRendering rendering = new SiteRendering(sys, conf);
        rendering.updateLangs(langs);
        rendering.setTargetHome(targetPath);
        
        // 执行
        rendering.render();
    }

}
