package org.nutz.walnut.cheap.dom.match;

import org.nutz.walnut.cheap.dom.CheapElement;
import org.nutz.walnut.cheap.dom.CheapMatcher;
import org.nutz.walnut.util.Ws;

public class CheapAutoMatcher implements CheapMatcher {

    private CheapMatcher ma;

    public CheapAutoMatcher(String str) {
        // Attr: [xxx]
        if (Ws.isQuoteBy(str, '[', ']')) {
            ma = new CheapAttrMatcher(str);
        }
        // Class: .xxx
        else if (str.startsWith(".")) {
            ma = new CheapClassNameMatcher(str);
        }
        // TagName
        else if (str.startsWith("^")) {
            ma = new CheapRegexTagNameMatcher(str);
        }
        // TagName
        else {
            ma = new CheapTagNameMatcher(str);
        }
    }

    @Override
    public boolean match(CheapElement el) {
        if (null != ma) {
            return ma.match(el);
        }
        return false;
    }

}
