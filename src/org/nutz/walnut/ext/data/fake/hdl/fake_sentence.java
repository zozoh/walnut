package org.nutz.walnut.ext.data.fake.hdl;

import org.nutz.walnut.ext.data.fake.FakeContext;
import org.nutz.walnut.ext.data.fake.FakeFilter;
import org.nutz.walnut.ext.data.fake.impl.WnSentenceFaker;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.ZParams;

public class fake_sentence extends FakeFilter {

    @Override
    protected void process(WnSystem sys, FakeContext fc, ZParams params) {
        String lang = sys.getLang();
        lang = params.get("lang", lang);
        lang = Ws.snakeCase(lang);

        // 默认
        if (params.vals.length == 0) {
            fc.faker = new WnSentenceFaker(lang);
        }
        // 数值范围
        else if (params.vals.length == 1) {
            String s = params.val(0);
            fc.faker = new WnSentenceFaker(lang, s);
        }
        // 两个值，分别表示 [最小值] 与 [最大值]
        else {
            int min = params.val_check_int(0);
            int max = params.val_check_int(1);
            fc.faker = new WnSentenceFaker(lang, min, max);
        }
    }

}
