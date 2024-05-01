package com.site0.walnut.ext.data.fake.impl;

import com.site0.walnut.ext.data.fake.WnFaker;

public class WnCaseLowerFaker extends WnCaseFaker {

    public WnCaseLowerFaker(WnFaker<?> faker) {
        super(faker);
    }

    @Override
    protected String toCase(String s) {
        return s.toLowerCase();
    }

}
