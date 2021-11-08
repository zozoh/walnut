package org.nutz.walnut.ooml;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nutz.lang.Encoding;
import org.nutz.walnut.cheap.dom.CheapDocument;
import org.nutz.walnut.cheap.dom.CheapElement;
import org.nutz.walnut.cheap.xml.CheapXmlParsing;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.Wpath;

public class OomlRels {

    private String path;

    private Map<String, OomlRel> rels;

    public OomlRels() {
        rels = new HashMap<>();
    }

    public OomlRels(OomlEntry en) {
        this(en.getPath(), en.getContentStr());
    }

    public OomlRels(String path, String input) {
        this.path = path;
        rels = new HashMap<>();
        CheapDocument doc = new CheapDocument("Relationships");
        CheapXmlParsing parser = new CheapXmlParsing(doc);
        doc = parser.parseDoc(input);

        List<CheapElement> list = doc.findElements(el -> el.isTagName("Relationship"));
        for (CheapElement el : list) {
            OomlRel rel = new OomlRel();
            rel.setId(el.attr("Id"));
            rel.setTarget(el.attr("Target"));
            String type = el.attr("Type");
            rel.setType(type);
            rels.put(rel.getId(), rel);
        }
    }

    public CheapDocument toDocument() {
        CheapDocument doc = new CheapDocument("Relationships");
        CheapElement root = doc.root();
        root.attr("xmlns", "http://schemas.openxmlformats.org/package/2006/relationships");
        if (null != rels && !rels.isEmpty()) {
            for (OomlRel rel : rels.values()) {
                CheapElement el = doc.createElement("Relationship");
                el.attr("Id", rel.getId());
                el.attr("Type", rel.getType());
                el.attr("Target", rel.getTarget());
                root.append(el);
            }
        }
        return doc;
    }

    @Override
    public String toString() {
        CheapDocument doc = this.toDocument();
        StringBuilder sb = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
        sb.append(doc.toMarkup());
        return sb.toString();
    }

    public byte[] toByte() {
        return this.toByte(Encoding.CHARSET_UTF8);
    }

    public byte[] toByte(Charset charset) {
        String s = this.toString();
        return s.getBytes(charset);
    }

    /**
     * @param id
     *            目标 ID
     * @return 目标在包中的唯一路径
     */
    public String getTargetPath(String id) {
        OomlRel rel = rels.get(id);
        if (null != rel) {
            String target = rel.getTarget();
            return this.getUniqPath(target);
        }
        return null;
    }

    public String getUniqPath(String rph) {
        int pos = this.path.indexOf('/');
        String name = this.path.substring(0, pos);
        String ph = Wn.appendPath(name, rph);
        return Wpath.getCanonicalPath(ph);
    }

    public OomlRel get(String id) {
        return rels.get(id);
    }

    public String getTarget(String id) {
        OomlRel rel = rels.get(id);
        if (null != rel) {
            return rel.getTarget();
        }
        return null;
    }

    public OomlRel getBy(String type) {
        for (OomlRel rel : rels.values()) {
            if (rel.isType(type)) {
                return rel;
            }
        }
        return null;
    }

    public String getTargetBy(String type) {
        OomlRel rel = this.getBy(type);
        if (null != rel) {
            return rel.getTarget();
        }
        return null;
    }

    public String getPath() {
        return path;
    }

}
