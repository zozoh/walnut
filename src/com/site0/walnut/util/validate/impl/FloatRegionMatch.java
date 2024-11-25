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
        if (null == val)
            return false;

        // 归一输入参数为 Float
        float v;
        if (val instanceof Number) {
            v = ((Number) val).floatValue();
        } else {
            try {
                v = Float.parseFloat(val.toString());
            }
            catch (Throwable err) {
                return false;
            }
        }

        return region.match(v);

    }

}
