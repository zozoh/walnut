package com.site0.walnut.ext.data.fake.impl;

import com.site0.walnut.ext.data.fake.WnFaker;
import com.site0.walnut.util.Wuu;

public class WnUU32Faker implements WnFaker<String> {

    @Override
    public String next() {
        return Wuu.UU32();
    }

}
