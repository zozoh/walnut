package com.site0.walnut.ext.data.fake.hdl;

import com.site0.walnut.ext.data.fake.FakeContext;
import com.site0.walnut.ext.data.fake.FakeFilter;
import com.site0.walnut.ext.data.fake.impl.WnNameFakeMode;
import com.site0.walnut.ext.data.fake.impl.WnNameFaker;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

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
