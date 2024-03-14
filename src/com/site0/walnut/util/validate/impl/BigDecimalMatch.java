package com.site0.walnut.util.validate.impl;

import java.math.BigDecimal;

import com.site0.walnut.util.validate.WnMatch;

public class BigDecimalMatch implements WnMatch {

    private BigDecimal n;

    public BigDecimalMatch(BigDecimal n) {
        this.n = n;
    }

    @Override
    public boolean match(Object val) {
        if (null == val) {
            return false;
        }
        if (val instanceof BigDecimal) {
            return ((BigDecimal) val).equals(n);
        }
        if (val instanceof Long) {
            long d = ((Number) val).longValue();
            return d == n.longValue();
        }
        if (val instanceof Double) {
            double d = ((Number) val).doubleValue();
            return d == n.doubleValue();
        }
        if (val instanceof Number) {
            double d = ((Number) val).doubleValue();
            return d == n.doubleValue();
        }
        return false;
    }

}
