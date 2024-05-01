package com.site0.walnut.ext.data.fake.impl;

import com.site0.walnut.ext.data.fake.WnFaker;

public class WnCaseUpperFaker extends WnCaseFaker {

    public WnCaseUpperFaker(WnFaker<?> faker) {
        super(faker);
    }

    @Override
    protected String toCase(String s) {
        return s.toUpperCase();
    }

}
