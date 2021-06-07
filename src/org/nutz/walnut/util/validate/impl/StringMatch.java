package org.nutz.walnut.util.validate.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.validate.WnMatch;

public class StringMatch extends ListValueMatcher implements WnMatch {

    final static Pattern P = Pattern.compile("^(~~)?([+])?(.*)$");

    private boolean ignoreCase;

    private String str;

    public StringMatch(String str) {
        Matcher m = P.matcher(str);
        if (m.find()) {
            this.ignoreCase = "~~".equals(m.group(1));
            this.matchAll = "+".equals(m.group(2));
            this.str = Ws.trim(m.group(3));
        } else {
            this.ignoreCase = false;
            this.matchAll = false;
            this.str = str;
        }
    }

    @Override
    protected boolean __match_val(Object val) {
        if (ignoreCase) {
            return str.equalsIgnoreCase(val.toString());
        }

        return str.equals(val);
    }

    public boolean isIgnoreCase() {
        return ignoreCase;
    }

    public void setIgnoreCase(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }

    public String getStr() {
        return str;
    }

    public void setStr(String str) {
        this.str = str;
    }
}
