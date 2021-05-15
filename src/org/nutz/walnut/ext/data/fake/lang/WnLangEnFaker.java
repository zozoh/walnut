package org.nutz.walnut.ext.data.fake.lang;

import java.util.List;

import org.nutz.walnut.ext.data.fake.WnFakerLang;
import org.nutz.walnut.util.Ws;

public class WnLangEnFaker implements WnFakerLang {

    @Override
    public String firstWord(String word) {
        return Ws.upperFirst(word);
    }

    @Override
    public void joinWord(StringBuilder sb, String word) {
        if (sb.length() > 0) {
            sb.append(' ');
        }
        sb.append(word);
    }

    @Override
    public String joinSentence(List<String> words) {
        return Ws.join(words, " ") + ". ";
    }

    @Override
    public String joinName(String... names) {
        for (int i = 0; i < names.length; i++) {
            names[i] = Ws.upperFirst(names[i]);
        }
        return Ws.join(names, " ");
    }

}
