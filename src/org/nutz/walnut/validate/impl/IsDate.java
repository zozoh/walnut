package org.nutz.walnut.validate.impl;

import java.util.Date;

import org.nutz.walnut.validate.WnValidator;

public class IsDate implements WnValidator {

    @Override
    public boolean isTrue(Object val, Object[] args) {
        if(null == val) {
            return false;
        }
        return val instanceof Date;
    }

}