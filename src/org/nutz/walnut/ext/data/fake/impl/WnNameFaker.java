package org.nutz.walnut.ext.data.fake.impl;

import org.nutz.walnut.ext.data.fake.WnFakes;
import org.nutz.walnut.ext.data.fake.WnFaker;
import org.nutz.walnut.ext.data.fake.util.WnFakeWord;

public class WnNameFaker extends WnWordFaker implements WnFaker<String> {

    private WnFakeWord name0;

    private WnFakeWord name1;

    public WnNameFaker(String lang) {
        super(lang);
        WnFakes me = WnFakes.me();
        this.name0 = me.getWord(lang, WnFakes.TP_NAME0);
        this.name1 = me.getWord(lang, WnFakes.TP_NAME1);
    }

    @Override
    public String next() {
        String[] names = new String[2];
        names[0] = this.name0.next();
        names[1] = this.name1.next();
        return this.fakerLang.joinName(names);
    }

}
