package com.site0.walnut.util.validate.impl;

import com.site0.walnut.util.validate.WnMatch;

public class NilMatch implements WnMatch {

    @Override
    public boolean match(Object val) {
        return null == val;
    }

}
