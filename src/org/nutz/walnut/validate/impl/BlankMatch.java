package org.nutz.walnut.validate.impl;

import org.nutz.lang.Strings;
import org.nutz.walnut.validate.WnMatch;

public class BlankMatch implements WnMatch {

    @Override
    public boolean match(Object val) {
        if (null == val)
            return true;

        return Strings.isBlank(val.toString());
    }

}
