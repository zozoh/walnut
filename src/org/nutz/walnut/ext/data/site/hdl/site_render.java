package org.nutz.walnut.ext.data.site.hdl;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Stopwatch;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.data.site.SiteContext;
import org.nutz.walnut.ext.data.site.SiteFilter;
import org.nutz.walnut.ext.data.site.render.SitePageRenderConfig;
import org.nutz.walnut.ext.data.site.render.SiteRendering;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

public class site_render extends SiteFilter {

    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "cqn", "^(json|copy|page)$");
    }

    @Override
    protected void process(WnSystem sys, SiteContext fc, ZParams params) {
        // 分析参数
        String targetPath = params.val(0);
        String[] langs = params.getAs("lang", String[].class);
        String arId = params.getString("ar");

        // 计时
        Stopwatch sw = Stopwatch.begin();

        // 读取配置
        String confPath = params.getString("conf");
        WnObj oConf = Wn.checkObj(sys, confPath);
        String json = sys.io.readText(oConf);
        SitePageRenderConfig conf = Json.fromJson(SitePageRenderConfig.class, json);

        // 准备服务类
        SiteRendering rendering = new SiteRendering(sys, conf);
        rendering.setJsonMode(params.is("json"));
        rendering.setArchiveId(arId);
        rendering.setWillCopyFiles(params.is("copy"));
        rendering.setWillRenderPages(params.is("page"));
        rendering.updateSiteHome(fc.oSite);
        rendering.updateLangs(langs);
        rendering.updateTargetHome(targetPath);

        // 执行
        rendering.render();

        sw.stop();
        rendering.LOGf("All done in %s", sw);

        // 输出结果
        if (rendering.hasResults()) {
            JsonFormat jfmt = Cmds.gen_json_format(params);
            String str = Json.toJson(rendering.getResults(), jfmt);
            sys.out.println(str);
        }
    }

}
