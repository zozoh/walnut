package org.nutz.walnut.validate.impl;

import org.nutz.walnut.validate.WnMatch;

public class IntegerMatch implements WnMatch {

    private int n;

    public IntegerMatch(int n) {
        this.n = n;
    }

    @Override
    public boolean match(Object val) {
        if (null == val) {
            return false;
        }
        if (val instanceof Number) {
            return ((Number) val).intValue() == this.n;
        }
        try {
            int v = Integer.parseInt(val.toString());
            return v == this.n;
        }
        catch (Exception e) {}
        return false;
    }

}
