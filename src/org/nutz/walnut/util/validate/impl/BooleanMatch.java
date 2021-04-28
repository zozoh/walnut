package org.nutz.walnut.util.validate.impl;

import org.nutz.castor.Castors;
import org.nutz.walnut.util.validate.WnMatch;

public class BooleanMatch implements WnMatch {

    private boolean expect;

    public BooleanMatch(boolean input) {
        this.expect = input;
    }

    @Override
    public boolean match(Object val) {
        Boolean b = Castors.me().castTo(val, Boolean.class);
        if (null == b) {
            return false == expect;
        }
        return this.expect == b;
    }

}
