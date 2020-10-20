package org.nutz.walnut.validate.impl;

import org.nutz.lang.util.Region;
import org.nutz.walnut.validate.WnMatch;

public class IntRegionMatch implements WnMatch {

    private Region<Integer> region = null;

    public IntRegionMatch(String input) {
        region = Region.Int(input);
    }

    @Override
    public boolean match(Object val) {
        if (null == val || !(val instanceof Number))
            return false;

        Number n = (Number) val;
        int v = n.intValue();
        if ((int) n.longValue() != v) {
            return false;
        }

        return region.match(v);

    }

}
