package org.nutz.walnut.ext.site.hdl;

import java.io.File;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.nutz.lang.Each;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Stopwatch;
import org.nutz.lang.util.Disks;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.site.ShCtx;
import org.nutz.walnut.ext.site.SiteHdl;
import org.nutz.walnut.ext.site.jsoup.JsoupHelper;
import org.nutz.walnut.impl.box.WnSystem;

public class ShRender implements SiteHdl {

    @Override
    public void invoke(WnSystem sys, ShCtx sc) throws Exception {
        sys.out.printlnf("site will render %d items", sc.args.length);
        Stopwatch sw = Stopwatch.begin();
        for (String ph : sc.args) {
            WnObj o = sys.io.check(sc.oCurrent, ph);
            __do_render_obj(sys, sc, o);
        }
        sw.stop();
        sys.out.println("All Done! " + sw);
    }

    private void __do_html(WnSystem sys, ShCtx sc, WnObj o) {
        // 找到对象的模板
        Document tmpl = sc.getTemplateDom();

        // 读取
        sys.out.print("r");
        String html = sys.io.readText(o);

        // 解析
        sys.out.print("P");
        Document doc = Jsoup.parse(html);

        // 展开组件
        sys.out.print("E");
        JsoupHelper helper = new JsoupHelper(sc, tmpl, doc);
        helper.extendDoc();

        // 输出临时文件
        sys.out.print("D");
        File fTmp = Files.createFileIfNoExists2("~/tmp/walnut/dom/" + o.name());
        Files.write(fTmp, helper.getHtml());

        // 执行渲染
        sys.out.print("R");

        // 整理 DOM 结构
        sys.out.print("T");

        // 最终输出
        sys.out.print("O");

        // 搞定
        sys.out.println(" .. OK");
    }

    private void __do_render_obj(final WnSystem sys, final ShCtx sc, WnObj o) {
        sc._rph = Disks.getRelativePath(sc.oHome.path(), o.path());

        sys.out.printf("%4s : %s ... ", o.race(), sc._rph);

        // 如果是目录
        if (o.isDIR()) {
            sys.out.println("enter");
            sc.sys.io.eachChildren(o, null, new Each<WnObj>() {
                public void invoke(int index, WnObj child, int length) {
                    __do_render_obj(sys, sc, child);
                }
            });
        }
        // 如果是 HTML 文件
        else if (o.isType("html")) {
            __do_html(sys, sc, o);
        }
        // 如果是 markdown 文件
        else if (o.isType("md")) {
            throw Lang.noImplement();
        }
        // 如果是其他文件
        else {
            throw Lang.noImplement();
        }
    }

}
