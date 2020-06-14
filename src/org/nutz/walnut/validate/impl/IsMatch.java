package org.nutz.walnut.validate.impl;

import java.util.Map;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.validate.WnValidator;

public class IsMatch implements WnValidator {

    @SuppressWarnings("unchecked")
    @Override
    public boolean isTrue(Object val, Object[] args) {
        if (null == val || args.length == 0) {
            return false;
        }
        if (!(val instanceof Map)) {
            return false;
        }
        if (!(args[0] instanceof Map)) {
            return false;
        }

        NutMap vMap = NutMap.WRAP((Map<String, Object>) val);
        NutMap aMap = NutMap.WRAP((Map<String, Object>) args[0]);

        return aMap.match(vMap);
    }

}
