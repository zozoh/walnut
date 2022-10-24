package org.nutz.walnut.ext.media.ooml.explain;

import java.util.List;

import org.nutz.lang.Encoding;
import org.nutz.lang.util.NutBean;
import org.nutz.walnut.cheap.api.CheapResourceLoader;
import org.nutz.walnut.cheap.dom.CheapDocument;
import org.nutz.walnut.cheap.dom.CheapElement;
import org.nutz.walnut.ext.media.ooml.api.OomlExplaining;
import org.nutz.walnut.ooml.OomlEntry;
import org.nutz.walnut.ooml.OomlPackage;
import org.nutz.walnut.ooml.Oomls;

public class WnOomlDocxExplaining2 implements OomlExplaining {

    private OomlPackage ooml;

    private CheapResourceLoader loader;

    public WnOomlDocxExplaining2(OomlPackage ooml, CheapResourceLoader loader) {
        this.ooml = ooml;
        this.loader = loader;
    }

    private void processElement(CheapElement el, NutBean vars, OomlEntry en) {
        // TODO Auto-generated method stub

    }

    private void processXML(OomlEntry en, NutBean vars) {
        // 加载实体对应的 XML
        CheapDocument doc = Oomls.parseEntryAsXml(en);

        // 从根节点开始处理
        this.processElement(doc.root(), vars, en);

        // 保存实体内容
        String xml = doc.toMarkup();
        byte[] buf = xml.getBytes(Encoding.CHARSET_UTF8);
        en.setContent(buf);
    }

    @Override
    public void explain(NutBean vars) {
        // 处理主文档
        OomlEntry en = ooml.getEntry("word/document.xml");
        this.processXML(en, vars);

        // 处理页眉
        List<OomlEntry> list = ooml.findEntriesByPath("^word/header\\d+.xml$");
        for (OomlEntry li : list) {
            this.processXML(li, vars);
        }

        // 处理页脚
        list = ooml.findEntriesByPath("^word/footer\\d+.xml$");
        for (OomlEntry li : list) {
            this.processXML(li, vars);
        }

        // 将修改的缓存内容(rels/ contentTypes) 等，写入到实体集合里
        ooml.saveAllRelationshipsFromCache();
        ooml.saveContentTypes();
    }

}
