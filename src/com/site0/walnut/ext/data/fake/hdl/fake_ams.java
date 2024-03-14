package com.site0.walnut.ext.data.fake.hdl;

import com.site0.walnut.ext.data.fake.FakeContext;
import com.site0.walnut.ext.data.fake.FakeFilter;
import com.site0.walnut.ext.data.fake.impl.WnAmsFaker;
import com.site0.walnut.ext.data.fake.impl.WnDateFormatFaker;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

public class fake_ams extends FakeFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "i");
    }

    @Override
    protected void process(WnSystem sys, FakeContext fc, ZParams params) {
        String v0 = params.val(0, "today");
        String v1 = params.val(1, "5m");
        boolean autoIncrease = params.is("i");

        WnAmsFaker faker = new WnAmsFaker(v0, v1, autoIncrease);
        String as = params.getString("as", "AMS");

        // 输出绝对毫秒数
        if ("AMS".equals(as)) {
            fc.faker = faker;
        }
        // 输出格式化的日期时间字符串
        else {
            fc.faker = new WnDateFormatFaker(faker, as);
        }

    }

}
