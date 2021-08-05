package org.nutz.walnut.util.bean.val;

import org.nutz.walnut.util.Wtime;
import org.nutz.walnut.util.bean.WnValue;

public class WnAMSValueAdaptor implements WnValueAdaptor {

    @Override
    public Object toValue(WnValue vd, Object input) {
        if (null == input) {
            return vd.getDefaultAs();
        }
        // 尝试解析
        try {
            return Wtime.parseAnyAMS(input);
        }
        // 解析不成功！ 当作不存在
        catch (Throwable e) {
            return vd.getDefaultAs();
        }
    }

//    @Override
//    public String toStr(WnValue vd, Object val) {
//        if (null == val) {
//            return null;
//        }
//        Date d = Wtime.parseAnyDate(val);
//        String fmt = Ws.sBlank(vd.getFormat(), "yyyy-MM-ddTHH:mm:ss.SSS");
//        return Times.format(fmt, d);
//    }

}
