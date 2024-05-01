package com.site0.walnut.ext.data.fake.impl;

import com.site0.walnut.ext.data.fake.WnFaker;
import com.site0.walnut.util.Ws;

public class WnCaseSnakeFaker extends WnCaseFaker {

    public WnCaseSnakeFaker(WnFaker<?> faker) {
        super(faker);
    }

    @Override
    protected String toCase(String s) {
        return Ws.snakeCase(s);
    }

}
