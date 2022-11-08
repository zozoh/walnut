package org.nutz.walnut.util.validate.impl;

import org.nutz.walnut.util.validate.WnMatch;

public class IntegerMatch implements WnMatch {

    private int n;

    public IntegerMatch(int n) {
        this.n = n;
    }

    @Override
    public boolean match(Object val) {
        if (null == val)
            return false;

        int v;
        if (!(val instanceof Number)) {
            try {
                v = Integer.parseInt(val.toString());
            }
            catch (Throwable e) {
                return false;
            }
        } else {
            Number n = (Number) val;
            v = n.intValue();
        }
        return this.n == v;
    }

}
