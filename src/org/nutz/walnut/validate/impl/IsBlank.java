package org.nutz.walnut.validate.impl;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

import org.nutz.lang.Strings;
import org.nutz.walnut.validate.WnValidator;

public class IsBlank implements WnValidator {

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
            return Strings.isBlank((CharSequence) val);
        }

        return false;
    }

}
