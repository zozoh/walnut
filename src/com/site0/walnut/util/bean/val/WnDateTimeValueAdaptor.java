package com.site0.walnut.util.bean.val;

import java.util.Date;

import org.nutz.lang.util.NutBean;

import com.site0.walnut.util.Ws;
import com.site0.walnut.util.Wtime;
import com.site0.walnut.util.bean.WnValue;
import com.site0.walnut.util.bean.WnValues;

public class WnDateTimeValueAdaptor implements WnValueAdaptor {

    @Override
    public Object toValue(WnValue vd, Object input, NutBean bean) {
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
            Date d = WnValues.parseDate(input, vd.getDatePrefix());
            String fmt = Ws.sBlank(vd.getFormat(), "yyyy-MM-dd'T'HH:mm:ss");
            return Wtime.format(d, fmt);
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
    // String fmt = Ws.sBlank(vd.getFormat(), "yyyy-MM-dd'T'HH:mm:ss");
    // return Wtime.format(d, fmt);
    // }
}
