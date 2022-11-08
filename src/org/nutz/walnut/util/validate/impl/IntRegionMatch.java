package org.nutz.walnut.util.validate.impl;

import org.nutz.lang.util.Region;
import org.nutz.walnut.util.validate.WnMatch;

public class IntRegionMatch implements WnMatch {

    private Region<Integer> region = null;

    public IntRegionMatch(String input) {
        region = Region.Int(input);
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

        return region.match(v);

    }

}
