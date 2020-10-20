package org.nutz.walnut.validate.match;

import org.nutz.walnut.validate.WnMatch;

public class NullMatch implements WnMatch {

    @Override
    public boolean match(Object val) {
        return null == val;
    }

}
