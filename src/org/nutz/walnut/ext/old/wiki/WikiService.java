package org.nutz.walnut.ext.old.wiki;

import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;

import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Each;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.Xmls;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.plugins.zdoc.NutD;
import org.nutz.plugins.zdoc.NutDSet;
import org.nutz.plugins.zdoc.NutDoc;
import org.nutz.plugins.zdoc.markdown.MarkdownDocParser;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@IocBean
public class WikiService {
    
    private static final Log log = Logs.get();

    @Inject
    protected WnIo io;

    public void tree(WnObj root, NutDSet dset, boolean withContent) throws Exception {
        // if root is dir, search for [tree.xml, tree.md, tree.json]
        WnObj tmp = null;
        if (root.isDIR()) {
            tmp = io.fetch(root, "tree.xml");
            if (tmp == null) {
                tmp = io.fetch(root, "tree.md");
            }
            if (tmp == null) {
                tmp = io.fetch(root, "index.xml");
            }
            if (tmp == null) {
                tmp = io.fetch(root, "tree.json");
            }
            // none of [tree.xml, tree.md, tree.json] found, use struct of dir
            if (tmp == null) {

            }
        } else {
            tmp = root;
        }
        if (tmp.name().endsWith(".xml")) {
            dset.setPrimerObj(io.get(tmp.parentId()));
            try (InputStream ins = io.getInputStream(tmp, 0)) {
                readTreeXml(ins, dset);
            }
        }
        if (withContent) {
            // 依次处理
            for (NutD d : dset.getChildren()) {
                __parse_it(d);
            }
        }
    }
    
    private void __parse_it(NutD d) {
        // 如果是 Markdown 文件，则解析
        if (d instanceof NutDoc) {
            MarkdownDocParser dp = new MarkdownDocParser();
            NutDoc doc = (NutDoc) d;
            log.info(" - parse : " + doc.getPath());
            dp.parse(doc);
        }
        // 如果是目录，则递归
        else if (d instanceof NutDSet) {
            for (NutD d2 : ((NutDSet) d).getChildren()) {
                __parse_it(d2);
            }
        }
        // 不可能
        else {
            throw Lang.impossible();
        }
    }

    // ----------------------------------------------------------
    // Read Tree define document as XML
    protected void readTreeXml(InputStream ins, NutDSet dset) throws Exception {
        Document root = Xmls.xmls().parse(ins);
        root.normalizeDocument();
        Element topEle = root.getDocumentElement();
        if (!"doc".equals(topEle.getNodeName())) {
            throw new RuntimeException("top xml node isn't doc!!!");
        }
        xmlEle2Node(topEle, dset);
    }

    protected void xmlEle2Node(Element ele, NutDSet dset) {
        WnObj dir = (WnObj) dset.getPrimerObj();
        if (ele.hasAttribute("title"))
            dset.setTitle(ele.getAttribute("title"));
        if (ele.hasAttribute("author"))
            dset.addAuthors(ele.getAttribute("author"));
        Xmls.eachChildren(ele, new Each<Element>() {
            public void invoke(int index, Element ele2, int length) {
                if (!"doc".equals(ele2.getNodeName()))
                    return;
                String path = ele2.getAttribute("path");
                if (Strings.isBlank(path))
                    return;
                WnObj wobj = io.fetch(dir, path);
                if (wobj == null) {
                    log.info("not exists path=" + dir.path() + "/" + path);
                    return;
                }
                if (wobj.isFILE()) {
                    NutDoc doc = dset.createDocByPath(ele2.getAttribute("path"), true);
                    if (ele2.hasAttribute("author"))
                        doc.addAuthors(ele2.getAttribute("author"));
                    if (ele2.hasAttribute("title"))
                        doc.setTitle(ele2.getAttribute("title"));
                    doc.setPrimerObj(wobj);
                    doc.setPrimerContent(io.readText(wobj));
                }
                else if (wobj.isDIR()) {
                    NutDSet next = dset.createSetByPath(path, true);
                    if (ele2.hasAttribute("author"))
                        next.addAuthors(ele2.getAttribute("author"));
                    if (ele2.hasAttribute("title"))
                        next.setTitle(ele2.getAttribute("title"));
                    next.setPrimerObj(wobj);
                    xmlEle2Node(ele2, next);
                }
                
            }
        });
        
    }

    // ---------------------------------------------------------------------

    public void render(Reader r, Writer w, NutMap confs) {

    }

//    public static void main(String[] args) throws Exception {
//        try (InputStream ins = Http.get("https://gitee.com/nutz/nutz/raw/master/doc/manual/index.xml")
//                                   .getStream()) {
//            NutDSet dset = new NutDSet("nutz");
//            new WikiService().readTreeXml(ins, dset);
//            System.out.println(Json.toJson(top));
//        }
//    }
}
