package org.nutz.walnut.validate.impl;

import org.nutz.walnut.validate.WnValidator;

public class IsNumber implements WnValidator {

    @Override
    public boolean isTrue(Object val, Object[] args) {
        if (null == val) {
            return false;
        }
        return (val instanceof Number);
    }

}
