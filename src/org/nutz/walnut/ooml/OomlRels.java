package org.nutz.walnut.ooml;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nutz.walnut.cheap.dom.CheapDocument;
import org.nutz.walnut.cheap.dom.CheapElement;
import org.nutz.walnut.cheap.html.CheapHtmlParsing;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.Wpath;

public class OomlRels {

    private static final Map<String, OomlRelType> TYPES = new HashMap<>();

    static {
        String prefix = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/";
        TYPES.put(prefix + "drawing", OomlRelType.DRAWING);
        TYPES.put(prefix + "image", OomlRelType.IMAGE);
        TYPES.put(prefix + "sharedStrings", OomlRelType.SHARED_STRING);
        TYPES.put(prefix + "styles", OomlRelType.STYLES);
        TYPES.put(prefix + "worksheet", OomlRelType.WORKSHEET);
    }

    private String path;

    private Map<String, OomlRelationship> rels;

    public OomlRels(String path, String input) {
        this.path = path;
        rels = new HashMap<>();
        CheapDocument doc = new CheapDocument("Relationships", null);
        CheapHtmlParsing parser = new CheapHtmlParsing(doc, null);
        doc = parser.invoke(input);

        List<CheapElement> list = doc.findElements(el -> el.isTagName("Relationship"));
        for (CheapElement el : list) {
            OomlRelationship rel = new OomlRelationship();
            rel.setId(el.attr("Id"));
            rel.setTarget(el.attr("Target"));
            String type = el.attr("Type");
            OomlRelType relType = TYPES.get(type);
            rel.setType(relType);
            rels.put(rel.getId(), rel);
        }
    }

    /**
     * @param id
     *            目标 ID
     * @return 目标在包中的唯一路径
     */
    public String getTargetPath(String id) {
        OomlRelationship rel = rels.get(id);
        if (null != rel) {
            String target = rel.getTarget();
            return this.getUniqPath(target);
        }
        return null;
    }

    public String getUniqPath(String rph) {
        String pph = Wpath.getParent(this.path);
        String ph = Wn.appendPath(pph, rph);
        return Wpath.getCanonicalPath(ph);
    }

    public OomlRelationship get(String id) {
        return rels.get(id);
    }

    public String getTarget(String id) {
        OomlRelationship rel = rels.get(id);
        if (null != rel) {
            return rel.getTarget();
        }
        return null;
    }

    public OomlRelationship getBy(OomlRelType type) {
        for (OomlRelationship rel : rels.values()) {
            if (rel.isType(type)) {
                return rel;
            }
        }
        return null;
    }

    public String getTargetBy(OomlRelType type) {
        OomlRelationship rel = this.getBy(type);
        if (null != rel) {
            return rel.getTarget();
        }
        return null;
    }

}
