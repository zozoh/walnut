package org.nutz.walnut.cheap.dom.docx;

import org.nutz.walnut.cheap.dom.CheapElement;

public abstract class Docxs {

    public static CheapElement genElement(String tagName, String attrName, Object attrValue) {
        CheapElement el = new CheapElement(tagName);
        el.setClosed(true);
        el.attr(attrName, attrValue);
        return el;
    }

    public static CheapElement genElVal(String tagName, Object attrValue) {
        return genElement(tagName, "w:val", attrValue);
    }

}
