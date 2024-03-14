package com.site0.walnut.ext.data.fake.impl;

import com.site0.walnut.ext.data.fake.WnFaker;
import com.site0.walnut.util.Wuu;

public class WnIntegerFaker extends FakeIntRange implements WnFaker<Integer> {

    public WnIntegerFaker() {
        this(0, 100);
    }

    public WnIntegerFaker(String input) {
        super(input);
    }

    public WnIntegerFaker(int min, int max) {
        super(min, max);
    }

    @Override
    public Integer next() {
        return Wuu.random(min, max);
    }

}
