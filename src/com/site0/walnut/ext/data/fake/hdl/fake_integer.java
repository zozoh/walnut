package com.site0.walnut.ext.data.fake.hdl;

import com.site0.walnut.ext.data.fake.FakeContext;
import com.site0.walnut.ext.data.fake.FakeFilter;
import com.site0.walnut.ext.data.fake.impl.WnIntegerFaker;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

public class fake_integer extends FakeFilter {

    @Override
    protected void process(WnSystem sys, FakeContext fc, ZParams params) {
        // 默认
        if (params.vals.length == 0) {
            fc.faker = new WnIntegerFaker();
        }
        // 数值范围
        else if (params.vals.length == 1) {
            String s = params.val(0);
            fc.faker = new WnIntegerFaker(s);
        }
        // 两个值，分别表示 [最小值] 与 [最大值]
        else {
            int min = params.val_check_int(0);
            int max = params.val_check_int(1);
            fc.faker = new WnIntegerFaker(min, max);
        }

    }

}
