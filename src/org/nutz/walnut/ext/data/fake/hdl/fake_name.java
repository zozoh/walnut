package org.nutz.walnut.ext.data.fake.hdl;

import org.nutz.walnut.ext.data.fake.FakeContext;
import org.nutz.walnut.ext.data.fake.FakeFilter;
import org.nutz.walnut.ext.data.fake.impl.WnNameFakeMode;
import org.nutz.walnut.ext.data.fake.impl.WnNameFaker;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.ZParams;

public class fake_name extends FakeFilter {

    @Override
    protected void process(WnSystem sys, FakeContext fc, ZParams params) {
        String lang = sys.getLang();
        lang = params.getString("lang", lang);
        lang = Ws.snakeCase(lang);
        String mode = params.val(0, "full");
        mode = mode.toUpperCase();

        WnNameFakeMode fm = WnNameFakeMode.valueOf(mode);

        fc.faker = new WnNameFaker(lang, fm);
    }

}
