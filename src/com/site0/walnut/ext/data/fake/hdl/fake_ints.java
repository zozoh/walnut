package com.site0.walnut.ext.data.fake.hdl;

import com.site0.walnut.ext.data.fake.FakeContext;
import com.site0.walnut.ext.data.fake.FakeFilter;
import com.site0.walnut.ext.data.fake.impl.WnIntTmplFaker;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

public class fake_ints extends FakeFilter {

    @Override
    protected void process(WnSystem sys, FakeContext fc, ZParams params) {
        String tmpl = params.val_check(0);
        fc.faker = new WnIntTmplFaker(tmpl);
    }

}
