package org.nutz.walnut.validate.impl;

import org.nutz.walnut.validate.WnMatch;

public class DoubleMatch implements WnMatch {

    private double n;

    public DoubleMatch(double n) {
        this.n = n;
    }

    @Override
    public boolean match(Object val) {
        if (null == val) {
            return false;
        }
        if (val instanceof Number) {
            return ((Number) val).doubleValue() == this.n;
        }
        try {
            double v = Double.parseDouble(val.toString());
            return v == this.n;
        }
        catch (Exception e) {}
        return false;
    }

}
