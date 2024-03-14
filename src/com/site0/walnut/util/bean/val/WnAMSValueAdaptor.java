package com.site0.walnut.util.bean.val;

import java.util.Date;

import com.site0.walnut.util.bean.WnValue;
import com.site0.walnut.util.bean.WnValues;

public class WnAMSValueAdaptor implements WnValueAdaptor {

    @Override
    public Object toValue(WnValue vd, Object input) {
        if (null == input) {
            return vd.getDefaultAs();
        }
        if (input instanceof Number) {
            long ams = ((Number) input).longValue();
            if (ams <= 0) {
                return vd.getDefaultAs();
            }
        }
        // 尝试解析
        try {
            String datePrefix = vd.getDatePrefix();
            Date d = WnValues.parseDate(input, datePrefix);
            return d.getTime();
        }
        // 解析不成功！ 当作不存在
        catch (Throwable e) {
            return vd.getDefaultAs();
        }
    }

    // @Override
    // public String toStr(WnValue vd, Object val) {
    // if (null == val) {
    // return null;
    // }
    // Date d = Wtime.parseAnyDate(val);
    // String fmt = Ws.sBlank(vd.getFormat(), "yyyy-MM-ddTHH:mm:ss.SSS");
    // return Times.format(fmt, d);
    // }

}
