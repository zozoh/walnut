package com.site0.walnut.cheap.dom.match;

import java.util.regex.Pattern;

import org.nutz.lang.util.Regex;
import com.site0.walnut.cheap.dom.CheapElement;
import com.site0.walnut.cheap.dom.CheapMatcher;

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
