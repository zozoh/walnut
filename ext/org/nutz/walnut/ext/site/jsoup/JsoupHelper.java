package org.nutz.walnut.ext.site.jsoup;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.nutz.lang.Strings;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.ext.site.ShCtx;

public class JsoupHelper {

    private ShCtx sc;

    private Document doc_tmpl;

    private Document doc_refer;

    private Element library;

    public JsoupHelper(ShCtx sc, Document tmpl, Document doc) {
        this.sc = sc;
        this.doc_tmpl = tmpl;
        this.doc_refer = doc;

        Element body = tmpl.body();

        // 创建组件输出的 DOM
        library = tmpl.select("section.library").first();
        if (null == library) {
            library = body.prependElement("section").addClass("library");
        }
    }

    public Element getLibrary(String name) {
        Element re = library.getElementsByAttributeValue("name", name).first();
        if (null == re) {
            Document doc = sc.getLibraryDom(name);
            // 第一个子节点一定是一个 <section>
            re = doc.body().child(0);
            if (!re.tagName().equals("section")) {
                throw Er.create("e.cmd.site.lib.wrongfmt", name);
            }
            re.attr("name", name);
            library.appendChild(re);
        }
        return re;
    }

    public void extend_lib(Element gEle, Elements exts, Element gasket) {
        // 删除旧的扩展点
        for (Element child : gEle.children()) {
            if (child.hasAttr("extend")) {
                child.remove();
            }
        }

        // 执行扩展
        for (Element ext : exts) {
            // 检查组件
            String libName = ext.attr("apply");

            if (Strings.isBlank(libName)) {
                throw Er.create("e.cmd.site.ext.blankLibName", ext.outerHtml());
            }

            Element libDom = this.getLibrary(libName);

            // 组件如果有扩展点，则扩展
            Element libTmpl = libDom.select(".lib-dom").first();
            extend_gaskets(libTmpl, ext, ext);

            // 附加到扩展点
            gasket.appendChild(ext);
        }
    }

    public void extend_gaskets(Element tmpl, Element gasket, Element ref) {
        // 遍历模板上的所有的扩展点
        Elements gEles = tmpl.getElementsByAttribute("gasket");
        for (Element gEle : gEles) {
            String gasketName = gEle.attr("gasket");

            // 从文档里摘取对应的扩展
            Elements exts = __get_extends(gasketName, ref, gEle);

            // 展开这个扩展点
            if (!exts.isEmpty()) {
                extend_lib(gEle, exts, null == gasket ? gEle : gasket);
            }
        }
    }

    private Elements __get_extends(String gasketName, Element... refs) {
        for (Element ref : refs) {
            Elements list = __get_extends_from_children(gasketName, ref);
            if (null != list)
                return list;
        }
        return new Elements();
    }

    private Elements __get_extends_from_children(String gasketName, Element p) {
        Elements list = null;
        for (Element ext : p.children()) {
            if (gasketName.equals(ext.attr("extend"))) {
                if (null == list)
                    list = new Elements();
                list.add(ext);
            }
        }
        return list;
    }

    public void extendDoc() {
        extend_gaskets(doc_tmpl.body(), null, doc_refer.body());
        // 整理文档的头
        Element head = doc_tmpl.head().empty();
        head.append("<meta charset='UTF-8'>");
        for (String key : sc.jsLibs.keySet()) {
            head.append("<script src='" + key + "'></script>");
        }
        head.append("<script>$(function(){$site.renderPage();});</script>");
    }

    public String getHtml() {
        return doc_tmpl.toString();
    }

}
