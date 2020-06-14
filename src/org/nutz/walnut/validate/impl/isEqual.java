package org.nutz.walnut.validate.impl;

import org.nutz.lang.Lang;
import org.nutz.walnut.validate.WnValidator;

public class isEqual implements WnValidator {

    @Override
    public boolean isTrue(Object val, Object[] args) {
        if (null == val || args.length == 0) {
            return false;
        }

        return Lang.equals(val, args[0]);
    }

}
