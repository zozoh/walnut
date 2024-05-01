package com.site0.walnut.ext.data.fake.impl;

import com.site0.walnut.ext.data.fake.WnFaker;
import com.site0.walnut.util.Ws;

public class WnCaseCamelFaker extends WnCaseFaker {

    public WnCaseCamelFaker(WnFaker<?> faker) {
        super(faker);
    }

    @Override
    protected String toCase(String s) {
        return Ws.camelCase(s);
    }

}
