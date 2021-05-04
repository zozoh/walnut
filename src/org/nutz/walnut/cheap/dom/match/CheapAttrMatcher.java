package org.nutz.walnut.cheap.dom.match;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.walnut.cheap.dom.CheapElement;
import org.nutz.walnut.cheap.dom.CheapMatcher;
import org.nutz.walnut.util.Ws;

public class CheapAttrMatcher implements CheapMatcher {

    static String R0 = "^\\[([^=\\]]+)(=([^\\]]+))?\\]$";
    private static final Pattern P0 = Pattern.compile(R0);

    private String attrName;

    private String attrValue;

    public CheapAttrMatcher(String str) {
        Matcher m = P0.matcher(str);
        if (m.find()) {
            this.attrName = m.group(1);
            String val = m.group(3);
            if (null == val) {
                this.attrValue = val;
            } else if (Ws.isQuoteBy(val, '\'', '\'')) {
                this.attrValue = val.substring(1, val.length() - 1);
            } else if (Ws.isQuoteBy(val, '"', '"')) {
                this.attrValue = val.substring(1, val.length() - 1);
            } else {
                this.attrValue = val;
            }
        }
    }

    public CheapAttrMatcher(String attrName, String attrValue) {
        this.attrName = attrName;
        this.attrValue = attrValue;
    }

    public String toString() {
        if (null == attrValue) {
            return String.format("[%s]", attrName);
        }
        return String.format("[%s='%s']", attrName, attrValue);
    }

    @Override
    public boolean match(CheapElement el) {
        if (null == attrName) {
            return false;
        }

        if (!el.hasAttr(attrName)) {
            return false;
        }

        if (null == attrValue)
            return true;

        return el.isAttr(attrName, attrValue);
    }

}
