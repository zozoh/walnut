package com.site0.walnut.ext.data.site.hdl;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Stopwatch;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.data.site.SiteContext;
import com.site0.walnut.ext.data.site.SiteFilter;
import com.site0.walnut.ext.data.site.render.SitePageRenderConfig;
import com.site0.walnut.ext.data.site.render.SiteRendering;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.ZParams;

public class site_render extends SiteFilter {

    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "cqnr", "^(json|copy|page)$");
    }

    @Override
    protected void process(WnSystem sys, SiteContext fc, ZParams params) {
        // 分析参数

        String[] langs = params.getAs("lang", String[].class);
        String targetPath = params.getString("target", null);
        String arName = params.getString("name", null);
        String markKey = params.getString("mark", null);

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
        rendering.setArchiveIds(params.vals);
        rendering.setAppJsonPath(params.getString("app", null));
        rendering.setArchiveSetName(arName);
        rendering.setWillCopyFiles(params.is("copy"));
        rendering.setWillRenderPages(params.is("page"));
        rendering.updateSiteHome(fc.oSite);
        rendering.setMarkKey(markKey);
        rendering.setWillRecur(params.is("r"));
        rendering.updateLangs(langs);
        rendering.updateTargetHome(targetPath);
        rendering.setBefore(params.getString("before"));
        rendering.setAfter(params.getString("after"));
        rendering.setRun(sys);

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
