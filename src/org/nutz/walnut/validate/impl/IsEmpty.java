package org.nutz.walnut.validate.impl;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

import org.nutz.walnut.validate.WnValidator;

public class IsEmpty implements WnValidator {

    @Override
    public boolean isTrue(Object val, Object[] args) {
        if (null == val)
            return true;

        if (val instanceof Collection<?>) {
            return ((Collection<?>) val).size() <= 0;
        }

        if (val.getClass().isArray()) {
            return Array.getLength(val) <= 0;
        }

        if (val instanceof Map<?, ?>) {
            return ((Map<?, ?>) val).size() <= 0;
        }

        if (val instanceof CharSequence) {
            return ((CharSequence) val).length() <= 0;
        }

        return false;
    }

}
