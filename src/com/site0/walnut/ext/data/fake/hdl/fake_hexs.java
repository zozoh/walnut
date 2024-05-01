package com.site0.walnut.ext.data.fake.hdl;

import com.site0.walnut.ext.data.fake.FakeContext;
import com.site0.walnut.ext.data.fake.FakeFilter;
import com.site0.walnut.ext.data.fake.impl.WnHexTmplFaker;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

public class fake_hexs extends FakeFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(upper)$");
    }

    @Override
    protected void process(WnSystem sys, FakeContext fc, ZParams params) {
        boolean upper = params.is("upper");
        String tmpl = params.val_check(0);
        fc.faker = new WnHexTmplFaker(tmpl, upper);
    }

}
