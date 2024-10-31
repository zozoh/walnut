package com.site0.walnut.util.bean.val;

import org.nutz.castor.Castors;
import org.nutz.lang.util.NutBean;

import com.site0.walnut.util.bean.WnValue;

public class WnFloatValueAdaptor implements WnValueAdaptor {

    @Override
    public Object toValue(WnValue fld, Object input, NutBean bean) {
        if (null == input) {
            return fld.getDefaultAs();
        }
        try {
            double v = Castors.me().castTo(input, Double.class);
            if (!fld.isDoubleInRegion(v)) {
                return fld.getDefaultAs();
            }

            // 处理单位
            double d = fld.getUnit();
            if (d > 0) {
                v = v / d;
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

}
