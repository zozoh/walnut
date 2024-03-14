package com.site0.walnut.cheap.dom.docx;

import com.site0.walnut.cheap.dom.CheapElement;
import com.site0.walnut.util.validate.WnMatch;
import com.site0.walnut.util.validate.impl.AutoStrMatch;

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
