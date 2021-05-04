package org.nutz.walnut.cheap.dom.match;

import org.nutz.walnut.cheap.dom.CheapElement;
import org.nutz.walnut.cheap.dom.CheapMatcher;

public class CheapTagNameMatcher implements CheapMatcher {

    private boolean isAny;

    private String upperTagName;

    private String tagName;

    public CheapTagNameMatcher(String str) {
        this.isAny = "*".equals(str);
        this.tagName = str.toLowerCase();
        this.upperTagName = str.toUpperCase();
    }

    public String toString() {
        return tagName;
    }

    @Override
    public boolean match(CheapElement el) {
        if (isAny) {
            return true;
        }
        return el.isStdTagName(upperTagName);
    }

}
