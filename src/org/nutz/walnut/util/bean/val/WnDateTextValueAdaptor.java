package org.nutz.walnut.util.bean.val;

import java.util.Date;

import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.Wtime;
import org.nutz.walnut.util.bean.WnValue;
import org.nutz.walnut.util.bean.WnValues;

public class WnDateTextValueAdaptor implements WnValueAdaptor {

    @Override
    public Object toValue(WnValue vd, Object input) {
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
