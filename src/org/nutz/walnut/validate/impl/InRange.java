package org.nutz.walnut.validate.impl;

import org.nutz.castor.Castors;
import org.nutz.lang.util.Region;
import org.nutz.walnut.validate.WnValidator;

public class InRange implements WnValidator {

    @Override
    public boolean isTrue(Object val, Object[] args) {
        if (null == val || args.length == 0) {
            return false;
        }

        Castors ca = Castors.me();
        double n = ca.castTo(val, double.class);

        if (args.length == 1) {
            Region<Double> rg = Region.Double(args[0].toString());
            return rg.match(n);
        }

        double min = ca.castTo(args[0], double.class);
        double max = ca.castTo(args[1], double.class);
        return n >= min && n <= max;
    }

}
