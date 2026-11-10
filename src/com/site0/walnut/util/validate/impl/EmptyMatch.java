package com.site0.walnut.util.validate.impl;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

import com.site0.walnut.util.Ws;
import com.site0.walnut.util.validate.WnMatch;

public class EmptyMatch implements WnMatch {

    @Override
    public boolean match(Object val) {
        if (null == val) {
            return true;
        }
        if (val instanceof Map<?, ?>) {
            return ((Map<?, ?>) val).isEmpty();
        }
        if (val instanceof Collection<?>) {
            return ((Collection<?>) val).isEmpty();
        }
        if (val.getClass().isArray()) {
            return 0 == Array.getLength(val);
        }
        return Ws.isEmpty(val.toString());
    }

}
