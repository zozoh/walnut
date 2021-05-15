package org.nutz.walnut.ext.data.fake;

import java.util.List;

public interface WnFakerLang {

    String firstWord(String word);

    void joinWord(StringBuilder sb, String word);

    String joinSentence(List<String> words);

    String joinName(String... names);
}
