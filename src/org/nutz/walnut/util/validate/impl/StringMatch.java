package org.nutz.walnut.util.validate.impl;

import org.nutz.walnut.util.validate.WnMatch;

public class StringMatch implements WnMatch {

    private boolean ignoreCase;

    private String str;

    public StringMatch(String str) {
        if (str.startsWith("~~")) {
            ignoreCase = true;
            this.str = str.substring(2);
        } else {
            ignoreCase = false;
            this.str = str;
        }
    }

    @Override
    public boolean match(Object val) {
        if (null == str) {
            return null == val;
        }
        if (null == val)
            return false;

        if (ignoreCase) {
            return str.equalsIgnoreCase(val.toString());
        }

        return str.equals(val);
    }

}
