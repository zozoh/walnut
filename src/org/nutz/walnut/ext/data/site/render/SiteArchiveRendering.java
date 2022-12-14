package org.nutz.walnut.ext.data.site.render;

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

    void renderArchives() {
        // 防守
        if (!this.oArHome.isDIR()) {
            throw Er.create("e.site.render.ArHomeNotDir", oArHome);
        }
        ing.LOGf("Render: %s", ar.getBase());
        this.renderChildren(oArHome);
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
        rph = Files.renameSuffix(rph, ".html");
        ctx.put("rph", rph);
        ing.LOGf("%d) %s", ing.I++, rph);

        if (ing.hasLangs()) {
            for (String lang : ing.getLangs()) {
                ctx.put("lang", lang);
                __write_dist_html(ctx);
            }
        } else {
            __write_dist_html(ctx);
        }

    }

    @SuppressWarnings("unchecked")
    private void __write_dist_html(NutMap ctx) {
        // 输出目标
        String ph = Tmpl.exec(ar.getDist(), ctx);
        WnObj oTa = ing.createTargetFile(ph);
        ing.LOGf(" - write to : %s", ph);

        // 准备上下文
        NutMap context;
        Object re = Wn.explainObj(ctx, ar.getVars());
        if (null == re) {
            context = new NutMap();
        } else {
            context = NutMap.WRAP((Map<String, Object>) re);
        }
        WWW.joinWWWContext(context, ing.targetHome);

        // 渲染
        String input = ing.getWnmlInput();
        String html = ing.wnmls.invoke(ing.wnmlRuntime, context, input);

        // 写入结果
        ing.io.writeText(oTa, html);
    }

    private WnQuery genQuery(WnObj oP) {
        WnQuery q = Wn.Q.pid(oP);
        if (ar.hasFilter()) {
            q.setAll(ar.getFilter());
        }
        q.sort(Wlang.map("nm", 1));
        q.limit(2000);
        return q;
    }
}
