package org.nutz.walnut.validate.impl;

import org.nutz.walnut.validate.WnMatch;

public class NotMatch implements WnMatch {

    private WnMatch m;

    public NotMatch(WnMatch m) {
        this.m = m;
    }

    @Override
    public boolean match(Object val) {
        return !this.m.match(val);
    }

}
