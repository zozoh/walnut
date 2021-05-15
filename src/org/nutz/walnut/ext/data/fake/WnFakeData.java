package org.nutz.walnut.ext.data.fake;

import java.util.HashMap;
import java.util.Map;
import org.nutz.lang.Files;
import org.nutz.walnut.ext.data.fake.lang.WnLangEnFaker;
import org.nutz.walnut.ext.data.fake.lang.WnLangZhFaker;
import org.nutz.walnut.util.Wlang;

public class WnFakeData {

    private static Map<String, WnFakerLang> langFakers;

    static {
        langFakers = new HashMap<>();
        langFakers.put("zh_cn", new WnLangZhFaker());
        langFakers.put("en_us", new WnLangEnFaker());
    }

    public WnFakerLang getLang(String lang) {
        return langFakers.get(lang);
    }

    public static final String TP_NAME0 = "name0";
    public static final String TP_NAME1 = "name1";
    public static final String TP_WORDS = "words";

    private static WnFakeData _me = null;

    public static WnFakeData me() {
        if (null == _me) {
            _me = new WnFakeData();
        }
        return _me;
    }

    private Map<String, Map<String, WnFakeWord>> dicts;

    public WnFakeData() {
        dicts = new HashMap<>();
        dicts.put("en_us", load("en_us"));
        dicts.put("zh_cn", load("zh_cn"));
    }

    Map<String, WnFakeWord> load(String lang) {
        Map<String, WnFakeWord> re = new HashMap<>();
        String pkg = WnFakeData.class.getPackage().getName().replace('.', '/');
        String base = pkg + "/data/" + lang + "/";
        String[] names = Wlang.array(TP_NAME0, TP_NAME1, TP_WORDS);
        for (String name : names) {
            String fph = base + name + ".txt";
            String str = Files.read(fph);
            WnFakeWord fw = new WnFakeWord(str);
            re.put(name, fw);
        }
        return re;
    }

    public WnFakeWord getWord(String lang, String type) {
        WnFakeWord fd = dicts.get(lang).get(type);
        return fd;
    }

    public String nextWord(String lang, String type) {
        WnFakeWord fd = getWord(lang, type);
        return fd.next();
    }

}
