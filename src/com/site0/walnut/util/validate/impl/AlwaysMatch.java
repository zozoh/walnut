package com.site0.walnut.util.validate.impl;

import com.site0.walnut.util.validate.WnMatch;

public class AlwaysMatch implements WnMatch {
    
    private boolean result;
    
    public AlwaysMatch(boolean result) {
        this.result = result;
    }

    @Override
    public boolean match(Object val) {
        return result;
    }

}
