package org.nutz.walnut.validate.match;

import org.nutz.castor.Castors;
import org.nutz.walnut.validate.WnMatch;

public class BooleanMatch implements WnMatch {

    private boolean expect;

    public BooleanMatch(boolean input) {
        this.expect = input;
    }

    @Override
    public boolean match(Object val) {
        Boolean b = Castors.me().castTo(val, Boolean.class);
        return this.expect == b;
    }

}
