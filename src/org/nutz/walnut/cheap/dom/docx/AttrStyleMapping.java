package org.nutz.walnut.cheap.dom.docx;

import org.nutz.walnut.cheap.dom.CheapElement;
import org.nutz.walnut.validate.WnMatch;
import org.nutz.walnut.validate.impl.AutoStrMatch;

class AttrStyleMapping {

    String attrName;

    WnMatch test;

    String styleId;

    public AttrStyleMapping(String attrName, String attrValue, String styleId) {
        this.attrName = attrName;
        this.test = new AutoStrMatch(attrValue);
        this.styleId = styleId;
    }

    public String tryGetStyle(CheapElement el) {
        String val = el.attr(attrName);
        if (null != val && test.match(val)) {
            return styleId;
        }
        return null;
    }

}
