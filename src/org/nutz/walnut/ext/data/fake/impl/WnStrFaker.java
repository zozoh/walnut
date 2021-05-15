package org.nutz.walnut.ext.data.fake.impl;

import org.nutz.walnut.ext.data.fake.WnFaker;
import org.nutz.walnut.util.Wuu;

public class WnStrFaker extends FakeIntRange implements WnFaker<String> {

    public WnStrFaker() {
        this(6, 12);
    }

    public WnStrFaker(String input) {
        super(input);
    }

    public WnStrFaker(int min, int max) {
        super(min, max);
    }

    @Override
    public String next() {
        return Wuu.nextStr(min, max);
    }

}
