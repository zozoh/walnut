package com.site0.walnut.ext.data.fake.hdl;

import com.site0.walnut.ext.data.fake.FakeContext;
import com.site0.walnut.ext.data.fake.FakeFilter;
import com.site0.walnut.ext.data.fake.impl.WnUU32Faker;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

public class fake_uu32 extends FakeFilter {

    @Override
    protected void process(WnSystem sys, FakeContext fc, ZParams params) {
        fc.faker = new WnUU32Faker();
    }

}
