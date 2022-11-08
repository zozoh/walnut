package org.nutz.walnut.util.validate.impl;

import org.nutz.walnut.util.validate.WnMatch;

public class LongMatch implements WnMatch {

    private long n;

    public LongMatch(long n) {
        this.n = n;
    }

    @Override
    public boolean match(Object val) {
        if (null == val)
            return false;

        long v;
        if (!(val instanceof Number)) {
            try {
                v = Long.parseLong(val.toString());
            }
            catch (Throwable e) {
                return false;
            }
        } else {
            Number n = (Number) val;
            v = n.longValue();
        }
        return this.n == v;
    }

}
