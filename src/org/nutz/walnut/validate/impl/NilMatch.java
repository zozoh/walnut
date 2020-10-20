package org.nutz.walnut.validate.impl;

import org.nutz.walnut.validate.WnMatch;

public class NilMatch implements WnMatch {

    @Override
    public boolean match(Object val) {
        return null == val;
    }

}
