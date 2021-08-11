package org.nutz.walnut.util.validate.impl;

import org.nutz.walnut.util.validate.WnMatch;

public class BooleanMatch implements WnMatch {

    private boolean expect;

    public BooleanMatch(boolean input) {
        this.expect = input;
    }

    @Override
    public boolean match(Object val) {
        if (null != val && (val instanceof Boolean)) {
            return expect == ((Boolean) val).booleanValue();
        }
        return false;
    }

}
