package com.site0.walnut.util.validate.impl;

import org.nutz.lang.util.Region;
import com.site0.walnut.util.validate.WnMatch;

public class LongRegionMatch implements WnMatch {

    private Region<Double> region = null;

    public LongRegionMatch(String input) {
        region = Region.Double(input);
    }

    @Override
    public boolean match(Object val) {
        if (null == val)
            return false;

        // 归一输入参数为 Float
        double v;
        if (val instanceof Number) {
            v = ((Number) val).doubleValue();
        } else {
            try {
                v = Double.parseDouble(val.toString());
            }
            catch (Throwable err) {
                return false;
            }
        }

        return region.match(v);
    }

}
