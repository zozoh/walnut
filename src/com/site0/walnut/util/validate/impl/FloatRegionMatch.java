package com.site0.walnut.util.validate.impl;

import org.nutz.lang.util.Region;
import com.site0.walnut.util.validate.WnMatch;

public class FloatRegionMatch implements WnMatch {

    private Region<Float> region = null;

    public FloatRegionMatch(String input) {
        region = Region.Float(input);
    }

    @Override
    public boolean match(Object val) {
        if (null == val || !(val instanceof Number))
            return false;

        Number n = (Number) val;
        float v = n.floatValue();

        return region.match(v);

    }

}
