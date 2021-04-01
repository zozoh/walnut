package org.nutz.walnut.cheap.dom.flt;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CheapRegexAttrNameFilter implements CheapAttrNameFilter {

    private Pattern pattern;

    private int group;

    private boolean dftAsNull;

    public CheapRegexAttrNameFilter(String regex) {
        this(regex, 1);
    }

    public CheapRegexAttrNameFilter(String regex, int group) {
        this(Pattern.compile(regex), group, false);
    }

    public CheapRegexAttrNameFilter(String regex, int group, boolean dftAsNull) {
        this(Pattern.compile(regex), group, dftAsNull);
    }

    public CheapRegexAttrNameFilter(Pattern pattern, int group, boolean dftAsNull) {
        this.pattern = pattern;
        this.group = group;
        this.dftAsNull = dftAsNull;
    }

    @Override
    public String getName(String name) {
        Matcher m = pattern.matcher(name);
        if (m.find()) {
            return m.group(group);
        }
        if (this.dftAsNull) {
            return null;
        }
        return name;
    }

}
