package org.nutz.walnut.validate.impl;

import org.nutz.lang.Strings;
import org.nutz.walnut.validate.WnMatch;

public class EmptyMatch implements WnMatch {

    @Override
    public boolean match(Object val) {
        if (null == val) {
            return true;
        }
        return Strings.isEmpty(val.toString());
    }

}
