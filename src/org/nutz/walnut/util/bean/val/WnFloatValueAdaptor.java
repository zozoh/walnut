package org.nutz.walnut.util.bean.val;

import org.nutz.castor.Castors;
import org.nutz.walnut.util.bean.WnValue;

public class WnFloatValueAdaptor implements WnValueAdaptor {

    @Override
    public Object toValue(WnValue fld, Object input) {
        if (null == input) {
            return fld.getDefaultAs();
        }
        try {
            double v = Castors.me().castTo(input, Double.class);
            if (!fld.isDoubleInRegion(v)) {
                return fld.getDefaultAs();
            }
            return v;
        }
        // 解析失败，采用默认
        catch (Throwable e) {
            return fld.getDefaultAs();
        }
    }

}
