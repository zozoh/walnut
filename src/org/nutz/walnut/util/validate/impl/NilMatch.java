package org.nutz.walnut.util.validate.impl;

import org.nutz.walnut.util.validate.WnMatch;

public class NilMatch implements WnMatch {

    @Override
    public boolean match(Object val) {
        return null == val;
    }

}
