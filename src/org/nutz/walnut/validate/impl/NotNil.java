package org.nutz.walnut.validate.impl;

import org.nutz.walnut.validate.WnValidator;

public class NotNil implements WnValidator {

    @Override
    public boolean isTrue(Object val, Object[] args) {
        return null != val;
    }

}