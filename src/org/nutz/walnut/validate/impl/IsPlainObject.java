package org.nutz.walnut.validate.impl;

import org.nutz.lang.Mirror;
import org.nutz.walnut.validate.WnValidator;

public class IsPlainObject implements WnValidator {

    @Override
    public boolean isTrue(Object val, Object[] args) {
        if (null == val) {
            return false;
        }
        Mirror<Object> mi = Mirror.me(val);
        return mi.isMap() || mi.isPojo();
    }

}
