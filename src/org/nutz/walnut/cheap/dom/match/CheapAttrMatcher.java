package org.nutz.walnut.cheap.dom.match;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.walnut.cheap.dom.CheapElement;
import org.nutz.walnut.cheap.dom.CheapMatcher;

public class CheapAttrMatcher implements CheapMatcher {

    static String R0 = "^\\[([\\w\\d_-]+)(=(.+))?\\]$";
    private static final Pattern P0 = Pattern.compile(R0);

    private String attrName;

    private String attrValue;

    public CheapAttrMatcher(String str) {
        Matcher m = P0.matcher(str);
        if (m.find()) {
            this.attrName = m.group(1);
            this.attrValue = m.group(3);
        }
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
