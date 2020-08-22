package org.nutz.walnut.validate.impl;

import java.util.Date;

import org.nutz.castor.Castors;
import org.nutz.lang.util.DateRegion;
import org.nutz.lang.util.DoubleRegion;
import org.nutz.lang.util.FloatRegion;
import org.nutz.lang.util.IntRegion;
import org.nutz.lang.util.LongRegion;
import org.nutz.lang.util.Region;
import org.nutz.lang.util.TimeRegion;
import org.nutz.walnut.validate.WnValidator;

public class InRange implements WnValidator {

    @Override
    public boolean isTrue(Object val, Object[] args) {
        if (null == val || args.length == 0) {
            return false;
        }

        // 已经是 Region 对象了
        if (args.length == 1) {
            Object arg0 = args[0];
            if (arg0 instanceof IntRegion) {
                IntRegion rg = (IntRegion) arg0;
                int v = Castors.me().castTo(val, int.class);
                return rg.match(v);
            }
            if (arg0 instanceof LongRegion) {
                LongRegion rg = (LongRegion) arg0;
                long v = Castors.me().castTo(val, long.class);
                return rg.match(v);
            }
            if (arg0 instanceof FloatRegion) {
                FloatRegion rg = (FloatRegion) arg0;
                float v = Castors.me().castTo(val, float.class);
                return rg.match(v);
            }
            if (arg0 instanceof DoubleRegion) {
                DoubleRegion rg = (DoubleRegion) arg0;
                double v = Castors.me().castTo(val, double.class);
                return rg.match(v);
            }
            if (arg0 instanceof DateRegion) {
                DateRegion rg = (DateRegion) arg0;
                Date v = Castors.me().castTo(val, Date.class);
                return rg.match(v);
            }
            if (arg0 instanceof TimeRegion) {
                TimeRegion rg = (TimeRegion) arg0;
                int v = Castors.me().castTo(val, int.class);
                return rg.match(v);
            }
        }

        // 尝试解析 Region 对象
        Castors ca = Castors.me();
        double n = ca.castTo(val, double.class);

        // 解析 Region
        if (args.length == 1 && args[0] instanceof CharSequence) {
            Region<Double> rg = Region.Double(args[0].toString());
            return rg.match(n);
        }

        // 就是两个数值
        double min = ca.castTo(args[0], double.class);
        double max = ca.castTo(args[1], double.class);
        return n >= min && n <= max;
    }

}
