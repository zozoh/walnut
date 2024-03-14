package com.site0.walnut.util.validate.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.lang.util.Regex;

import com.site0.walnut.util.validate.WnMatch;

public class RegexMatch extends ListValueMatcher implements WnMatch {

    final static Pattern P = Pattern.compile("^(!)?(~~)?([+])?(\\^.*)$");

    public static RegexMatch tryParse(String regex) {
        Matcher m = P.matcher(regex);
        if (m.find()) {
            boolean ignoreCase = "~~".equals(m.group(2));
            String str = m.group(4);
            Pattern ptn;
            if (ignoreCase) {
                ptn = Pattern.compile(str, Pattern.CASE_INSENSITIVE);
            } else {
                ptn = Regex.getPattern(str);
            }
            boolean isNot = "!".equals(m.group(1));
            RegexMatch r = new RegexMatch(ptn, isNot);
            r.matchAll = "+".equals(m.group(3));
            return r;
        }
        return null;
    }

    private Pattern ptn;

    private boolean not;

    private boolean ignoreCase;

    public RegexMatch(Pattern ptn, boolean not) {
        this.ptn = ptn;
        this.not = not;
        this.matchAll = false;
        this.ignoreCase = false;
    }

    public RegexMatch(String regex, boolean not) {
        this.ptn = Regex.getPattern(regex);
        this.not = not;
        this.matchAll = false;
        this.ignoreCase = false;
    }

    @Override
    protected boolean __match_val(Object val) {
        if (null == val) {
            return false;
        }
        Matcher m = ptn.matcher(val.toString());
        return m.find() ^ not;
    }

    public Pattern getPtn() {
        return ptn;
    }

    public void setPtn(Pattern ptn) {
        this.ptn = ptn;
    }

    public boolean isNot() {
        return not;
    }

    public void setNot(boolean not) {
        this.not = not;
    }

    public boolean isIgnoreCase() {
        return ignoreCase;
    }

    public void setIgnoreCase(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }

}
