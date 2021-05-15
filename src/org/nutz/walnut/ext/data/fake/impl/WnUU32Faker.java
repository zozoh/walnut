package org.nutz.walnut.ext.data.fake.impl;

import org.nutz.walnut.ext.data.fake.WnFaker;
import org.nutz.walnut.util.Wuu;

public class WnUU32Faker implements WnFaker<String> {

    @Override
    public String next() {
        return Wuu.UU32();
    }

}
