package org.nutz.walnut.util.validate.impl;

import org.nutz.lang.util.Region;
import org.nutz.walnut.util.validate.WnMatch;

public class LongRegionMatch implements WnMatch {

    private Region<Long> region = null;

    public LongRegionMatch(String input) {
        region = Region.Long(input);
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
        return region.match(v);

    }

}
