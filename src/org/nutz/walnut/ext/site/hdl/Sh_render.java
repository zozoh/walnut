package org.nutz.walnut.ext.site.hdl;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.nutz.lang.Each;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Stopwatch;
import org.nutz.lang.random.R;
import org.nutz.lang.util.Disks;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.site.ShCtx;
import org.nutz.walnut.ext.site.SiteHdl;
import org.nutz.walnut.ext.site.jsoup.JsoupHelper;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class Sh_render implements SiteHdl {

    private static final String _TMP_DIR = "~/.walnut/dom";

    private Map<String, String> _js_libs;

    private WebClient webc;

    public Sh_render() {
        webc = new WebClient();
    }

    private void __add_js_lib(WnSystem sys, String jsLibPath) {
        String ph = jsLibPath.startsWith("/") ? jsLibPath : "/rs/core/js/" + jsLibPath;
        WnObj o = sys.io.check(null, ph);
        _js_libs.put(o.name(), sys.io.readText(o));
    }

    @Override
    public void invoke(WnSystem sys, ShCtx sc) throws Exception {
        sys.out.printlnf("site will render %d items by:", sc.args.length);
        Stopwatch sw = Stopwatch.begin();
        ZParams params = ZParams.parse(sc.args, null);

        // 读取渲染时需要的 JS 库
        if (null == _js_libs) {
            _js_libs = new LinkedHashMap<String, String>();
            __add_js_lib(sys, "backbone/underscore-1.8.2/underscore.js");
            __add_js_lib(sys, "jquery/jquery-1.11.2/jquery-1.11.2.js");
            __add_js_lib(sys, "nutz/zutil.js");
            __add_js_lib(sys, "/app/wn/site/site_dom.js");
        }
        sc.jsLibs.putAll(_js_libs);
        for (String key : sc.jsLibs.keySet())
            sys.out.printlnf(" - %s", key);

        // 分配临时目录
        sc.dRenderHome = Files.createDirIfNoExists(_TMP_DIR + "/" + R.UU32());
        for (String key : sc.jsLibs.keySet()) {
            File f = Files.getFile(sc.dRenderHome, key);
            Files.createNewFile(f);
            Files.write(f, sc.jsLibs.get(key));
        }

        sys.out.printlnf("assign tmp dir: %s", sc.dRenderHome);

        // 处理每个渲染目标
        for (String ph : params.vals) {
            WnObj o = sys.io.check(sc.oCurrent, ph);
            __do_render_obj(sys, sc, o);
        }

        // 删除临时目录
        Files.deleteDir(sc.dRenderHome);
        sc.dRenderHome = null;

        // 如果声明了需要同步资源

        // 搞定定
        sw.stop();
        sys.out.println("All Done! " + sw);
    }

    private void __do_render_obj(final WnSystem sys, final ShCtx sc, WnObj o) {
        sc._rph = Disks.getRelativePath(sc.oHome.path(), o.path());

        sys.out.printf("%4s : %s ... ", o.race(), sc._rph);

        // 如果是目录
        if (o.isDIR()) {
            sys.out.println("enter");
            sys.io.each(Wn.Q.pid(o.id()), new Each<WnObj>() {
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

    private void __do_html(WnSystem sys, ShCtx sc, WnObj o) {
        // 准备计时器
        Stopwatch sw = Stopwatch.create();

        // 找到对象的模板
        Document tmpl = sc.getTemplateDom();

        // 为展开逻辑(js)标记关键信息
        Element tmplBody = tmpl.body();
        tmplBody.attr("site-path", sc.oHome.path());
        tmplBody.attr("page-path", o.path());
        tmplBody.attr("rpath", sc._rph);

        // 读取源文件
        sys.out.print("r");
        sw.start();
        String html = sys.io.readText(o);
        sw.stop();
        sys.out.printf(":%d> ", sw.getDuration());

        // 解析源文件
        sys.out.print("P");
        sw.start();
        Document doc = Jsoup.parse(html);
        sw.stop();
        sys.out.printf(":%d> ", sw.getDuration());

        // 展开组件
        sys.out.print("E");
        sw.start();
        JsoupHelper helper = new JsoupHelper(sc, tmpl, doc);
        helper.extendDoc();
        sw.stop();
        sys.out.printf(":%d> ", sw.getDuration());

        // 输出临时文件
        sys.out.print("D");
        sw.start();
        File fTmp = Files.getFile(sc.dRenderHome, "before_render.html");
        try {
            Files.createNewFile(fTmp);
        }
        catch (IOException e1) {
            throw Er.wrap(e1);
        }
        Files.write(fTmp, helper.getHtml());
        sw.stop();
        sys.out.printf(":%d> ", sw.getDuration());

        // 执行渲染
        sys.out.print("R");
        sw.start();
        HtmlPage page;
        try {
            page = webc.getPage("file://" + fTmp.getAbsolutePath());
        }
        catch (Exception e) {
            throw Er.wrap(e);
        }
        sw.stop();
        sys.out.printf(":%d> ", sw.getDuration());

        // 整理 DOM 结构
        sys.out.print("T");
        sw.start();
        File fOut = Files.getFile(sc.dRenderHome, "after_render.html");
        try {
            Files.createNewFile(fOut);
        }
        catch (IOException e1) {
            throw Er.wrap(e1);
        }
        String xml = page.asXml();
        if (xml.startsWith("<?xml")) {
            xml = xml.substring(xml.indexOf('\n') + 1);
        }
        Document out = Jsoup.parse(xml);
        Files.write(fOut, out.toString());
        sw.stop();
        sys.out.printf(":%d> ", sw.getDuration());

        // 最终输出
        sys.out.print("O");
        sw.start();
        WnObj oOut = sys.io.createIfNoExists(sc.oHome, ".tmp/publish/" + sc._rph, o.race());
        sys.io.writeText(oOut, out.toString());
        sw.stop();
        sys.out.printf(":%d> ", sw.getDuration());

        // 搞定
        sys.out.println(" .. OK");
    }

}
