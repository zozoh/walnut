package org.nutz.walnut.ext.data.fake.impl;

import org.nutz.walnut.ext.data.fake.WnFakeData;
import org.nutz.walnut.ext.data.fake.WnFakerLang;

public abstract class WnWordFaker {

    protected WnFakerLang fakerLang;

    protected String lang;

    public WnWordFaker(String lang) {
        this.lang = lang;
        this.fakerLang = WnFakeData.me().getLang(lang);
    }

}
