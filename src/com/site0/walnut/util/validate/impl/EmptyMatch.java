package com.site0.walnut.util.validate.impl;

import org.nutz.lang.Strings;
import com.site0.walnut.util.validate.WnMatch;

public class EmptyMatch implements WnMatch {

    @Override
    public boolean match(Object val) {
        if (null == val) {
            return true;
        }
        return Strings.isEmpty(val.toString());
    }

}
