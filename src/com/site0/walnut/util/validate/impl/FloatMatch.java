package com.site0.walnut.util.validate.impl;

import com.site0.walnut.util.validate.WnMatch;

public class FloatMatch implements WnMatch {

    private float n;

    public FloatMatch(float n) {
        this.n = n;
    }

    @Override
    public boolean match(Object val) {
        if (null == val) {
            return false;
        }
        if (val instanceof Number) {
            return ((Number) val).floatValue() == this.n;
        }
        try {
            float v = Float.parseFloat(val.toString());
            return v == this.n;
        }
        catch (Exception e) {}
        return false;
    }

}
