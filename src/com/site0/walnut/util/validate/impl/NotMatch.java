package com.site0.walnut.util.validate.impl;

import com.site0.walnut.util.validate.WnMatch;

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
