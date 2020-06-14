package org.nutz.walnut.validate.impl;

import org.nutz.castor.Castors;
import org.nutz.walnut.validate.WnValidator;

public class IsFalsy implements WnValidator {

    @Override
    public boolean isTrue(Object val, Object[] args) {
        if (null == val) {
            return false;
        }
        boolean re = Castors.me().castTo(val, Boolean.class);
        return re;
    }

}
