package org.nutz.walnut.ext.data.fake.hdl;

import org.nutz.walnut.ext.data.fake.FakeContext;
import org.nutz.walnut.ext.data.fake.FakeFilter;
import org.nutz.walnut.ext.data.fake.impl.WnAmsFaker;
import org.nutz.walnut.ext.data.fake.impl.WnFormatAmsFaker;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class fake_ams extends FakeFilter {

    @Override
    protected void process(WnSystem sys, FakeContext fc, ZParams params) {
        String v0 = params.val(0, "today");
        String v1 = params.val(1, "5m");

        WnAmsFaker faker = new WnAmsFaker(v0, v1, true);
        String as = params.getString("as", "AMS");

        // 输出绝对毫秒数
        if ("AMS".equals(as)) {
            fc.faker = faker;
        }
        // 输出格式化的日期时间字符串
        else {
            fc.faker = new WnFormatAmsFaker(faker, as);
        }

    }

}
