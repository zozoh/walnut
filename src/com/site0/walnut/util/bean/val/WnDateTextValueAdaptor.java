package com.site0.walnut.util.bean.val;

import java.util.Date;

import org.nutz.lang.util.NutBean;

import com.site0.walnut.util.Ws;
import com.site0.walnut.util.Wtime;
import com.site0.walnut.util.bean.WnValue;
import com.site0.walnut.util.bean.WnValues;

public class WnDateTextValueAdaptor implements WnValueAdaptor {

    @Override
    public Object toValue(WnValue vd, Object input, NutBean bean) {
        if (null == input) {
            return null;
        }
        Date d = WnValues.parseDate(input, vd.getDatePrefix());
        String fmt = Ws.sBlank(vd.getFormat(), "yyyy-MM-dd");
        return Wtime.format(d, fmt);
    }

    // @Override
    // public String toStr(WnValue vd, Object val) {
    // if (null == val) {
    // return null;
    // }
    // Date d = Wtime.parseAnyDate(val);
    // String fmt = Ws.sBlank(vd.getFormat(), "yyyy-MM-dd");
    // return Wtime.format(d, fmt);
    // }

}
