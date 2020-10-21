package org.nutz.walnut.validate.impl;

import org.nutz.walnut.validate.WnMatch;

public class LongMatch implements WnMatch {

    private long n;

    public LongMatch(long n) {
        this.n = n;
    }

    @Override
    public boolean match(Object val) {
        if (null == val) {
            return false;
        }
        if (val instanceof Number) {
            return ((Number) val).longValue() == this.n;
        }
        try {
            long v = Long.parseLong(val.toString());
            return v == this.n;
        }
        catch (Exception e) {}
        return false;
    }

}
