package com.site0.walnut.ext.data.fake.impl;

import com.site0.walnut.ext.data.fake.WnFaker;
import com.site0.walnut.util.Wuu;

public class WnHexFaker extends FakeIntRange implements WnFaker<String> {

    public WnHexFaker() {
        this(0, 0xFF);
    }

    public WnHexFaker(String input) {
        super(input, 16);
    }

    public WnHexFaker(int min, int max) {
        super(min, max);
    }

    @Override
    public String next() {
        int n = Wuu.random(min, max + 1);
        return Integer.toHexString(n);
    }

}
