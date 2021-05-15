package org.nutz.walnut.ext.data.fake.impl;

import java.util.ArrayList;
import java.util.List;

import org.nutz.walnut.ext.data.fake.WnFaker;
import org.nutz.walnut.util.Wuu;
import org.nutz.walnut.ext.data.fake.WnFakeData;
import org.nutz.walnut.ext.data.fake.WnFakeWord;

public class WnSentenceFaker extends WnWordFaker implements WnFaker<String> {

    private FakeIntRange ir;

    private WnFakeWord word;

    public WnSentenceFaker(String lang) {
        super(lang);
        this.ir = new FakeIntRange(5, 10);
        this.word = WnFakeData.me().getWord(lang, WnFakeData.TP_WORDS);
    }

    public WnSentenceFaker(String lang, String input) {
        super(lang);
        this.ir = new FakeIntRange(input);
        this.word = WnFakeData.me().getWord(lang, WnFakeData.TP_WORDS);
    }

    public WnSentenceFaker(String lang, int min, int max) {
        super(lang);
        this.ir = new FakeIntRange(min, max);
        this.word = WnFakeData.me().getWord(lang, WnFakeData.TP_WORDS);
    }

    @Override
    public String next() {
        int n = Wuu.random(ir.min, ir.max);
        List<String> list = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            String word = this.word.next();
            if (i == 0) {
                word = fakerLang.firstWord(word);
            }
            list.add(word);
        }
        return fakerLang.joinSentence(list);
    }

}
