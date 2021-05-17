package org.nutz.walnut.ext.data.fake.impl;

import org.nutz.walnut.ext.data.fake.WnFakes;
import org.nutz.walnut.ext.data.fake.WnFakerLang;

public abstract class WnWordFaker {

    protected WnFakerLang fakerLang;

    protected String lang;

    public WnWordFaker(String lang) {
        this.lang = lang;
        this.fakerLang = WnFakes.me().getLang(lang);
    }

}
