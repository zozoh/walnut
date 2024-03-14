package com.site0.walnut.cheap.dom.match;

import com.site0.walnut.cheap.dom.CheapElement;
import com.site0.walnut.cheap.dom.CheapMatcher;

public class CheapClassNameMatcher implements CheapMatcher {

    private String className;

    public CheapClassNameMatcher(String str) {
        if (str.startsWith(".")) {
            this.className = str.substring(1);
        } else {
            this.className = str;
        }
    }

    public String toString() {
        return "." + className;
    }

    @Override
    public boolean match(CheapElement el) {
        return el.hasClass(this.className);
    }

}
