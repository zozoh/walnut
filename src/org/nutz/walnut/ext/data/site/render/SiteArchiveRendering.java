package org.nutz.walnut.ext.data.site.render;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.nutz.lang.Each;
import org.nutz.lang.Files;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.ext.data.www.WWW;
import org.nutz.walnut.util.Wlang;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.tmpl.Tmpl;
import org.nutz.walnut.util.validate.WnMatch;
import org.nutz.walnut.util.validate.impl.AutoMatch;

public class SiteArchiveRendering {

    private SiteRendering ing;

    private SiteRenderArchive ar;

    private WnObj oArHome;

    private WnMatch canRecur;

    SiteArchiveRendering(SiteRendering ing, SiteRenderArchive ar) {
        this.ing = ing;
        this.oArHome = ing.checkObj(ar.getBase());
        this.ar = ar;
        this.canRecur = AutoMatch.parse(ar.getRecur());
    }

    void renderArchives(String... ids) {
        // 防守
        if (!this.oArHome.isDIR()) {
            throw Er.create("e.site.render.ArHomeNotDir", oArHome);
        }
        // 得到主目录的 path
        String homePath = this.oArHome.path();
        ing.LOGf("Render: %s", ar.getBase());

        if (ids.length > 0) {
            for (String id : ids) {
                WnObj oAr = ing.io.checkById(id);
                String arPath = oAr.path();
                if (arPath.startsWith(homePath)) {
                    this.renderArchive(oAr);
                } else {
                    ing.LOGf("Ignore [%s] cause out of [%s]", arPath, homePath);
                }
            }
        }
        // 整站渲染
        else {
            this.renderChildren(oArHome);
        }
    }

    void renderChildren(WnObj oPAr) {
        if (null == oPAr) {
            return;
        }
        WnQuery q = this.genQuery(oPAr);
        ing.io.each(q, new Each<WnObj>() {
            public void invoke(int index, WnObj oAr, int length) {
                renderArchive(oAr);
                if (canRecur.match(oAr)) {
                    renderChildren(oAr);
                }
            }
        });
    }

    void renderArchive(WnObj oAr) {
        // 目标输出路径
        NutMap ctx = new NutMap();
        ctx.putAll(oAr);
        String rph = Wn.Io.getRelativePath(oArHome, oAr);
        if (rph.endsWith("/")) {
            rph = rph.substring(0, rph.length() - 1) + ".html";
        } else {
            rph = Files.renameSuffix(rph, ".html");
        }
        ctx.put("rph", rph);
        ing.LOGf("%d) %s", ing.I++, rph);

        // 记录渲染的结果路径，这样再次渲染前，调用者有办法清除老的结果
        List<String> paths = new LinkedList<>();
        if (ing.hasLangs()) {
            for (String lang : ing.getLangs()) {
                ctx.put("lang", lang);
                String distPath = __write_dist_html(ctx);
                paths.add(distPath);
            }
        } else {
            String distPath = __write_dist_html(ctx);
            paths.add(distPath);
        }
        ing.addResult(oAr, paths);

    }

    @SuppressWarnings("unchecked")
    private String __write_dist_html(NutMap ctx) {
        // 输出目标
        String ph = Tmpl.exec(ar.getDist(), ctx);
        WnObj oTa = ing.createTargetFile(ph);
        ing.LOGf(" - write to : %s", ph);
        ctx.put("targetPath", ph);

        // 准备上下文
        NutMap context = ing.getGloabalVars();
        // 记入全局变量
        Object re = Wn.explainObj(ctx, ar.getVars());
        if (null != re) {
            context.putAll((Map<String, Object>) re);
        }
        context.put("URI_PATH", ph);
        WWW.joinWWWContext(context, ing.targetHome);

        // 渲染
        String input = ing.getWnmlInput();
        String html = ing.wnmls.invoke(ing.wnmlRuntime, context, input);

        // 写入结果
        ing.io.writeText(oTa, html);

        return ph;
    }

    private WnQuery genQuery(WnObj oP) {
        WnQuery q = Wn.Q.pid(oP);
        if (ar.hasFilter()) {
            q.setAll(ar.getFilter());
        }
        // 指定了排序方式
        if (ar.hasSort()) {
            q.sort(ar.getSort());
        }
        // 默认采用归档名排序
        else {
            q.sort(Wlang.map("nm", 1));
        }
        if (ar.getLimit() > 0) {
            q.limit(ar.getLimit());
        } else {
            q.limit(2000);
        }
        return q;
    }
}
