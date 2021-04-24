package org.nutz.walnut.util.bean.val;

import java.util.Date;

import org.nutz.castor.Castors;
import org.nutz.lang.Times;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.bean.WnValue;

public class WnAMSValueAdaptor implements WnValueAdaptor {

    @Override
    public Object toValue(WnValue vd, Object input) {
        if (null == input) {
            return vd.getDefaultAs();
        }
        String s = input.toString();

        // 就是一个毫秒数
        if (s.matches("^([0-9]+)$")) {
            return Castors.me().castTo(input, Long.class);
        }

        // 解析为日期
        try {
            Date d = Times.D(s);
            return d.getTime();
        }
        // 解析不成功！ 当作不存在
        catch (Throwable e) {
            return vd.getDefaultAs();
        }
    }

    @Override
    public String toStr(WnValue vd, Object val) {
        if (null == val) {
            return null;
        }
        long ams = Castors.me().castTo(val, Long.class);
        String fmt = Ws.sBlank(vd.getFormat(), "yyyy-MM-dd HH:mm:ss");
        Date d = Times.D(ams);
        return Times.format(fmt, d);
    }

}
