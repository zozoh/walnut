package org.nutz.walnut.ext.data.fake.impl;

import org.nutz.walnut.ext.data.fake.WnFakes;
import org.nutz.walnut.ext.data.fake.WnFaker;
import org.nutz.walnut.ext.data.fake.util.WnFakeWord;
import org.nutz.walnut.util.Wuu;

public class WnTextFaker extends WnWordFaker implements WnFaker<String> {

    private FakeIntRange ir;

    private WnFakeWord word;

    public WnTextFaker(String lang) {
        super(lang);
        this.ir = new FakeIntRange(20, 30);
        this.word = WnFakes.me().getWord(lang, WnFakes.TP_WORDS);
    }

    public WnTextFaker(String lang, String input) {
        super(lang);
        this.ir = new FakeIntRange(input);
        this.word = WnFakes.me().getWord(lang, WnFakes.TP_WORDS);
    }

    public WnTextFaker(String lang, int min, int max) {
        super(lang);
        this.ir = new FakeIntRange(min, max);
        this.word = WnFakes.me().getWord(lang, WnFakes.TP_WORDS);
    }

    @Override
    public String next() {
        int n = Wuu.random(ir.min, ir.max);
        StringBuilder sb = new StringBuilder();
        while (sb.length() < n) {
            String word = this.word.next();
            fakerLang.joinWord(sb, word);
        }
        if (sb.length() > n) {
            return sb.substring(0, n);
        }
        return sb.toString();
    }

}
