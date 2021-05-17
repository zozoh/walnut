package org.nutz.walnut.ext.data.fake;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.lang.Files;
import org.nutz.walnut.ext.data.fake.impl.WnAmsFaker;
import org.nutz.walnut.ext.data.fake.impl.WnDateFormatFaker;
import org.nutz.walnut.ext.data.fake.impl.WnEnumFaker;
import org.nutz.walnut.ext.data.fake.impl.WnIntTmplFaker;
import org.nutz.walnut.ext.data.fake.impl.WnIntegerFaker;
import org.nutz.walnut.ext.data.fake.impl.WnNameFaker;
import org.nutz.walnut.ext.data.fake.impl.WnSentenceFaker;
import org.nutz.walnut.ext.data.fake.impl.WnStaticFaker;
import org.nutz.walnut.ext.data.fake.impl.WnStrFaker;
import org.nutz.walnut.ext.data.fake.impl.WnStrTmplFaker;
import org.nutz.walnut.ext.data.fake.impl.WnTextFaker;
import org.nutz.walnut.ext.data.fake.impl.WnUU32Faker;
import org.nutz.walnut.ext.data.fake.lang.WnLangEnFaker;
import org.nutz.walnut.ext.data.fake.lang.WnLangZhFaker;
import org.nutz.walnut.ext.data.fake.util.WnFakeWord;
import org.nutz.walnut.util.Wlang;
import org.nutz.walnut.util.Ws;

public class WnFakes {

    private static String R_AMS = "^AMS([+=])?([^:]+)?(:([^:]+))?(:(.+))?$";
    private static Pattern P_AMS = Pattern.compile(R_AMS);

    /**
     * 根据字符串，构建一个 Faker 的工厂方法
     * 
     * @param input
     *            输入的字符串
     * @param lang
     *            语言。默认为 <code>zh_cn</code>
     * @return WnFaker 实现类
     */
    public static WnFaker<?> createFaker(String input, String lang) {
        // 生成 UU32
        // "UU32",
        if ("UU32".equals(input)) {
            return new WnUU32Faker();
        }
        // 生成 0-100 的整数
        // "INT",
        if ("INT".equals(input)) {
            return new WnIntegerFaker();
        }
        // 生成 50-100 的整数
        // "INT:50-100",
        if (input.startsWith("INT:")) {
            input = input.substring(4).trim();
            return new WnIntegerFaker(input);
        }
        // 生成 随机的多段整数
        // "INTS:192.168.{100-200}.{0-255}",
        if (input.startsWith("INTS:")) {
            input = input.substring(5).trim();
            return new WnIntTmplFaker(input);
        }
        // 生成随机的字符串
        // "STR",
        else if (input.startsWith("STR")) {
            return new WnStrFaker();
        }
        // 生成随机的字符串
        // "STR:5-10",
        if (input.startsWith("STR:")) {
            input = input.substring(5).trim();
            int pos = input.indexOf('-');
            int min = Integer.parseInt(input.substring(0, pos).trim());
            int max = Integer.parseInt(input.substring(pos + 1).trim());
            return new WnStrFaker(min, max);
        }
        // 生成随机的句子
        // "SENTENCE",
        if (input.startsWith("SENTENCE")) {
            return new WnSentenceFaker(lang);
        }
        // 生成随机的句子
        // "SENTENCE:5-10",
        else if (input.startsWith("SENTENCE:")) {
            input = input.substring(5).trim();
            int pos = input.indexOf('-');
            int min = Integer.parseInt(input.substring(0, pos).trim());
            int max = Integer.parseInt(input.substring(pos + 1).trim());
            return new WnSentenceFaker(lang, min, max);
        }
        // 生成随机的文本
        // "TEXT",
        if (input.startsWith("TEXT")) {
            return new WnTextFaker(lang);
        }
        // 生成随机的文本
        // "TEXT:5-10",
        if (input.startsWith("TEXT:")) {
            input = input.substring(5).trim();
            int pos = input.indexOf('-');
            int min = Integer.parseInt(input.substring(0, pos).trim());
            int max = Integer.parseInt(input.substring(pos + 1).trim());
            return new WnTextFaker(lang, min, max);
        }
        // 生成随机名称
        // "NAME"
        if ("NAME".equals(input)) {
            return new WnNameFaker(lang);
        }
        // 日期时间
        // 0/32 Regin:0/32
        // 0:[ 0, 32) `AMS+today:1d:yyyy-MM-dd HH:mm:ss`
        // 1:[ 3, 4) `+`
        // 2:[ 4, 9) `today`
        // 3:[ 9, 12) `:1d`
        // 4:[ 10, 12) `1d`
        // 5:[ 12, 32) `:yyyy-MM-dd HH:mm:ss`
        // 6:[ 13, 32) `yyyy-MM-dd HH:mm:ss`
        Matcher m = P_AMS.matcher(input);
        if (m.find()) {
            boolean autoIncrease = "+".equals(m.group(1));
            String start = Ws.sBlank(m.group(2), "today");
            String du = Ws.sBlank(m.group(4), "1d");
            String fmt = Ws.sBlank(m.group(6), null);
            WnAmsFaker faker = new WnAmsFaker(start, du, autoIncrease);
            if (null == fmt) {
                return faker;
            }
            return new WnDateFormatFaker(faker, fmt);
        }
        // 字符串模板
        if (input.startsWith(":")) {
            String s = input.substring(1).trim();
            return new WnStrTmplFaker(s, lang);
        }
        // 字符串枚举
        if (input.startsWith("?")) {
            String[] ss = Ws.splitIgnoreBlank(input.substring(1));
            return new WnEnumFaker(ss);
        }
        // 那就是静态字符串了
        return new WnStaticFaker(input);
    }

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

    private static WnFakes _me = null;

    public static WnFakes me() {
        if (null == _me) {
            _me = new WnFakes();
        }
        return _me;
    }

    private Map<String, Map<String, WnFakeWord>> dicts;

    public WnFakes() {
        dicts = new HashMap<>();
        dicts.put("en_us", load("en_us"));
        dicts.put("zh_cn", load("zh_cn"));
    }

    Map<String, WnFakeWord> load(String lang) {
        Map<String, WnFakeWord> re = new HashMap<>();
        String pkg = WnFakes.class.getPackage().getName().replace('.', '/');
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
