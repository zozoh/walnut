package org.nutz.walnut.ext.data.fake.impl;

import org.nutz.walnut.ext.data.fake.WnFakes;

import java.util.HashMap;
import java.util.Map;

import org.nutz.lang.random.R;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.data.fake.WnFaker;
import org.nutz.walnut.ext.data.fake.util.WnFakeWord;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.tmpl.Tmpl;

public class WnNameFaker extends WnWordFaker implements WnFaker<String> {

    private Map<String, WnFakeWord> words;

    private Tmpl tmpl;

    public WnNameFaker(String lang) {
        this(lang, WnNameFakeMode.FULL);
    }

    /**
     * @param lang
     *            语言
     * @param mode
     *            模式
     */
    public WnNameFaker(String lang, WnNameFakeMode mode) {
        super(lang);

        words = new HashMap<>();
        WnFakes me = WnFakes.me();
        tmpl = me.getNamePattern(lang);
        switch (mode) {
        case FIRST:
            words.put("FIRST", me.getWord(lang, WnFakes.TP_NAME_FIRST));
            break;
        case MIDDLE:
            words.put("M", me.getWord(lang, WnFakes.TP_NAME_MID));
            break;
        case FAMILY:
            words.put("FAMILY", me.getWord(lang, WnFakes.TP_NAME_MID));
            break;
        case FULL:
            words.put("FIRST", me.getWord(lang, WnFakes.TP_NAME_FIRST));
            words.put("FAMILY", me.getWord(lang, WnFakes.TP_NAME_MID));
            words.put("?M", me.getWord(lang, WnFakes.TP_NAME_MID));
            break;
        case FULL_NO_MIDDLE:
            words.put("FIRST", me.getWord(lang, WnFakes.TP_NAME_FIRST));
            words.put("FAMILY", me.getWord(lang, WnFakes.TP_NAME_MID));
            break;
        case FULL_WITH_MIDDLE:
        default:
            words.put("FIRST", me.getWord(lang, WnFakes.TP_NAME_FIRST));
            words.put("FAMILY", me.getWord(lang, WnFakes.TP_NAME_MID));
            words.put("M", me.getWord(lang, WnFakes.TP_NAME_MID));
        }
    }

    @Override
    public String next() {
        NutBean c = new NutMap();
        for (Map.Entry<String, WnFakeWord> en : words.entrySet()) {
            String key = en.getKey();
            if (key.startsWith("?")) {
                // 随机选择是否生成
                int n = R.random(0, 100);
                if (n > 50) {
                    continue;
                }
                key = key.substring(1);
            }
            WnFakeWord fw = en.getValue();
            String s = fw.next();
            
            c.put(key, Ws.upperFirst(s));
        }
        String re = tmpl.render(c);
        return re.replaceAll("[ ]+", " ").trim();
    }

}
