package org.nutz.walnut.ext.wiki;

import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;

import org.nutz.http.Http;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.json.Json;
import org.nutz.lang.Each;
import org.nutz.lang.Xmls;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@IocBean
public class WikiService {

    @Inject
    protected WnIo io;

    public WikiNode tree(WnObj root, NutMap confs) throws Exception {
        // if root is dir, search for [tree.xml, tree.md, tree.json]
        WnObj tmp = null;
        if (root.isDIR()) {
            tmp = io.fetch(root, "tree.xml");
            if (tmp == null) {
                tmp = io.fetch(root, "tree.md");
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
            try (InputStream ins = io.getInputStream(tmp, 0)) {
                return readTreeXml(ins);
            }
        }
        if (tmp.name().endsWith(".json")) {
            return io.readJson(tmp, WikiNode.class);
        }
        return null;
    }

    // ----------------------------------------------------------
    // Read Tree define document as XML
    protected WikiNode readTreeXml(InputStream ins) throws Exception {
        Document root = Xmls.xmls().parse(ins);
        root.normalizeDocument();
        Element topEle = root.getDocumentElement();
        WikiNode top = new WikiNode();
        if (!"doc".equals(topEle.getNodeName())) {
            throw new RuntimeException("top xml node isn't doc!!!");
        }
        xmlEle2Node(top, topEle);
        return top;
    }

    protected void xmlEle2Node(WikiNode node, Element ele) {
        node.path = ele.getAttribute("path");
        if (ele.hasAttribute("title"))
            node.title = ele.getAttribute("title");
        if (ele.hasAttribute("author"))
            node.author = ele.getAttribute("author");
        Xmls.eachChildren(ele, new Each<Element>() {
            public void invoke(int index, Element ele2, int length) {
                if (!"doc".equals(ele.getNodeName()))
                    return;
                WikiNode node2 = new WikiNode();
                if (node.subs == null)
                    node.subs = new ArrayList<>();
                node.subs.add(node2);
                xmlEle2Node(node2, ele2);
            }
        });
    }

    // ---------------------------------------------------------------------

    public void render(Reader r, Writer w, NutMap confs) {

    }

    public static void main(String[] args) throws Exception {
        try (InputStream ins = Http.get("https://gitee.com/nutz/nutz/raw/master/doc/manual/index.xml")
                                   .getStream()) {
            WikiNode top = new WikiService().readTreeXml(ins);
            System.out.println(Json.toJson(top));
        }
    }
}
