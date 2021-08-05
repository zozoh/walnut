package org.nutz.walnut.util.bean.val;

import java.util.Date;

import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.Wtime;
import org.nutz.walnut.util.bean.WnValue;

public class WnDateTimeValueAdaptor implements WnValueAdaptor {

    @Override
    public Object toValue(WnValue vd, Object input) {
        if (null == input) {
            return vd.getDefaultAs();
        }
        // 尝试解析
        try {
            Date d = Wtime.parseAnyDate(input);
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
