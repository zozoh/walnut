package com.site0.walnut.util.validate.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.site0.walnut.util.validate.WnMatch;

public class WildcardMatch implements WnMatch {

    private boolean not;

    private Pattern ptn;

    public WildcardMatch(String wildcard) {
        if (wildcard.startsWith("!")) {
            not = true;
            wildcard = wildcard.substring(1);
        }
        // 前置
        String regex = "^" + wildcard.replace("*", ".*") + "$";
        ptn = Pattern.compile(regex);
    }

    @Override
    public boolean match(Object val) {
        if (null == val) {
            return false;
        }
        Matcher m = ptn.matcher(val.toString());
        return m.find() ^ not;
    }

}
