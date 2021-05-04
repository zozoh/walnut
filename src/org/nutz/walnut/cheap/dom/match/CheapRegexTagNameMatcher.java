package org.nutz.walnut.cheap.dom.match;

import java.util.regex.Pattern;

import org.nutz.lang.util.Regex;
import org.nutz.walnut.cheap.dom.CheapElement;
import org.nutz.walnut.cheap.dom.CheapMatcher;

public class CheapRegexTagNameMatcher implements CheapMatcher {

    private Pattern tagName;

    public CheapRegexTagNameMatcher(String str) {
        this.tagName = Regex.getPattern(str.toUpperCase());
    }

    public String toString() {
        return tagName.toString();
    }

    @Override
    public boolean match(CheapElement el) {
        return this.tagName.matcher(el.getStdTagName()).find();
    }

}
