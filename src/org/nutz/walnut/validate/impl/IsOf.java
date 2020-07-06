package org.nutz.walnut.validate.impl;

import org.nutz.walnut.validate.WnValidator;

public class IsOf implements WnValidator {

    @Override
    public boolean isTrue(Object val, Object[] args) {
        if (null == val || args.length == 0)
            return false;

        for (Object arg : args) {
            if (val.equals(arg)) {
                return true;
            }
        }

        return false;
    }

}
