package org.nutz.walnut.cheap.dom.match;

import org.nutz.walnut.cheap.dom.CheapElement;
import org.nutz.walnut.cheap.dom.CheapMatcher;

public class CheapTagNameMatcher implements CheapMatcher {

    private String tagName;

    public CheapTagNameMatcher(String str) {
        this.tagName = str.toUpperCase();
    }

    @Override
    public boolean match(CheapElement el) {
        return el.isTagName(tagName);
    }

}
