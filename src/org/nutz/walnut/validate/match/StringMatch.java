package org.nutz.walnut.validate.match;

import org.nutz.walnut.validate.WnMatch;

public class StringMatch implements WnMatch {

    private String str;

    public StringMatch(String str) {
        this.str = str;
    }

    @Override
    public boolean match(Object val) {
        if (null == str) {
            return null == val;
        }
        if (null == val)
            return false;
        return str.equals(val);
    }

}
