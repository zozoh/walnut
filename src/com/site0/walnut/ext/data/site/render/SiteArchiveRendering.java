package com.site0.walnut.ext.data.site.render;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.nutz.lang.Each;
import org.nutz.lang.Files;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.ext.data.www.WWW;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.tmpl.WnTmpl;
import com.site0.walnut.util.validate.WnMatch;
import com.site0.walnut.util.validate.impl.AutoMatch;

public class SiteArchiveRendering {

    private SiteRendering ing;

    private SiteRenderArchive ar;

    private WnObj oArHome;

    private WnMatch canRecur;

    SiteArchiveRendering(SiteRendering ing, SiteRenderArchive ar) {
        this.ing = ing;
        this.oArHome = ing.checkObj(ar.getBase());
        // 重新获取依次，因为本地文件映射会修改路径
        // 我么需要最原始的路径，以便计算相对路径
        this.oArHome = ing.io.checkById(oArHome.id());
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
                if (ing.isWillRecur()) {
                    this.renderChildren(oAr);
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

        // 准备回调模板上下文
        NutMap runContext = new NutMap();
        runContext.put("home", ing.config.getHome());
        runContext.put("target", ing.config.getTarget());
        runContext.put("ar", ctx);
        runContext.put("vars", ing.config.getVars());

        // 渲染前回调
        if (ing.canRunCommand() && ing.hasBefore()) {
            String cmd = ing.getBeforeTmpl().render(runContext);
            ing.LOGf("  @BEFORE: %s", cmd);
            String re = ing.exec2(cmd);
            ing.LOGf("        >: %s", re);
        }

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

        // 标记到对象上
        if (ing.hasMarkKey()) {
            NutMap meta = Wlang.map(ing.getMarkKey(), paths);
            ing.io.appendMeta(oAr, meta);
            ing.LOGf(" + mark %s : %s", ing.getMarkKey(), Ws.join(paths, ", "));
        }

        ing.addResult(oAr, paths);

        // 渲染后回调
        if (ing.canRunCommand() && ing.hasAfter()) {
            String cmd = ing.getAfterTmpl().render(runContext);
            ing.LOGf("  @AFTER : %s", cmd);
            String re = ing.exec2(cmd);
            ing.LOGf("        >: %s", re);
        }

    }

    @SuppressWarnings("unchecked")
    private String __write_dist_html(NutMap ctx) {
        // 输出目标
        String ph = WnTmpl.exec(ar.getDist(), ctx);
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
        String aph = ph;
        if (!aph.startsWith("/")) {
            aph = "/" + aph;
        }
        context.put("URI_PATH", aph);
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
