package com.site0.walnut.ext.data.fake.lang;

import java.util.List;

import com.site0.walnut.ext.data.fake.WnFakerLang;
import com.site0.walnut.util.Ws;

public class WnLangZhFaker implements WnFakerLang {

    @Override
    public String firstWord(String word) {
        return word;
    }

    @Override
    public void joinWord(StringBuilder sb, String word) {
        sb.append(word);
    }

    @Override
    public String joinSentence(List<String> words) {
        return Ws.join(words, "") + "。";
    }

    @Override
    public String joinName(String... names) {
        return Ws.join(names, "");
    }

}
