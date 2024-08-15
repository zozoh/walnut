package com.site0.walnut.util.bean.val;

import org.nutz.castor.Castors;
import com.site0.walnut.util.bean.WnValue;

public class WnIntegerValueAdaptor implements WnValueAdaptor {

    @Override
    public Object toValue(WnValue fld, Object input) {
        if (null == input) {
            return fld.getDefaultAs();
        }

        try {
            int v = Castors.me().castTo(input, Integer.class);
            if (!fld.isIntInRegion(v)) {
                return fld.getDefaultAs();
            }
            // 处理单位
            double d = fld.getUnit();
            if (d > 0) {
                double f = (double) v;
                f = f / d;
                if (fld.hasFormat()) {
                    return String.format(fld.getFormat(), f);
                }
                return f;
            }

            // 格式化
            if (fld.hasFormat()) {
                return String.format(fld.getFormat(), v);
            }

            return v;
        }
        // 解析失败，采用默认
        catch (Throwable e) {
            return fld.getDefaultAs();
        }
    }

    // @Override
    // public String toStr(WnValue fld, Object val) {
    // return Castors.me().castToString(val);
    // }

}
