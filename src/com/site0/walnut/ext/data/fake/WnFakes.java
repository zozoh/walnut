package com.site0.walnut.ext.data.fake;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.lang.Files;

import com.site0.walnut.ext.data.fake.impl.WnAmsFaker;
import com.site0.walnut.ext.data.fake.impl.WnCaseFaker;
import com.site0.walnut.ext.data.fake.impl.WnDateFormatFaker;
import com.site0.walnut.ext.data.fake.impl.WnEnumFaker;
import com.site0.walnut.ext.data.fake.impl.WnHexFaker;
import com.site0.walnut.ext.data.fake.impl.WnHexTmplFaker;
import com.site0.walnut.ext.data.fake.impl.WnIntTmplFaker;
import com.site0.walnut.ext.data.fake.impl.WnIntegerFaker;
import com.site0.walnut.ext.data.fake.impl.WnNameFakeMode;
import com.site0.walnut.ext.data.fake.impl.WnNameFaker;
import com.site0.walnut.ext.data.fake.impl.WnPadFaker;
import com.site0.walnut.ext.data.fake.impl.WnSentenceFaker;
import com.site0.walnut.ext.data.fake.impl.WnStaticFaker;
import com.site0.walnut.ext.data.fake.impl.WnStrFaker;
import com.site0.walnut.ext.data.fake.impl.WnStrTmplFaker;
import com.site0.walnut.ext.data.fake.impl.WnSubStrFaker;
import com.site0.walnut.ext.data.fake.impl.WnTextFaker;
import com.site0.walnut.ext.data.fake.impl.WnUU32Faker;
import com.site0.walnut.ext.data.fake.lang.WnLangEnFaker;
import com.site0.walnut.ext.data.fake.lang.WnLangZhFaker;
import com.site0.walnut.ext.data.fake.util.WnFakeWord;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.tmpl.WnTmpl;

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
        // 针对 UPPER|LOWER|SNAKE|CAMEL|KEBAB 的伪造器
        Matcher m = WnCaseFaker.tryInput(input);
        if (m.find()) {
            return WnCaseFaker.createFaker(m, lang);
        }
        //
        // 针对 SUBSTR
        m = WnSubStrFaker.tryInput(input);
        if (m.find()) {
            return WnSubStrFaker.createFaker(m, lang);
        }
        /**
         * 根据输入字符串解析模拟配置
         * 
         * @param input
         * 
         *            <pre>
         *   +----范围
         *   |    +---- 居左还是居右，可以是 [LR]
         *   V    V
         * PAD[6R0]:{FAKER}
         *       ^ ^
         *       | +---- 采用什么字符填充，默认是 0 
         *       +--- 数字表示填充的宽度
         *            </pre>
         */
        if (input.startsWith("PAD[")) {
            int pos = input.indexOf("]:");
            if (pos > 0) {
                String pad = input.substring(0, pos + 1);
                String str = input.substring(pos + 2).trim();
                WnFaker<?> faker = createFaker(str, lang);
                WnPadFaker re = new WnPadFaker(faker);
                re.valueOf(pad, lang);
                return re;
            }
        }

        // 生成 UU32
        // "UU32",
        if ("UU32".equals(input)) {
            return new WnUU32Faker();
        }

        // 生成 随机的多段整数
        // "INTS:192.168.{100-200}.{0-255}",
        if (input.startsWith("INTS:")) {
            input = input.substring(5).trim();
            return new WnIntTmplFaker(input);
        }
        // 生成 50-100 的整数
        // "INT:50-100",
        if (input.startsWith("INT:")) {
            input = input.substring(4).trim();
            return new WnIntegerFaker(input);
        }
        // 生成 0-100 的整数
        // "INT",
        if ("INT".equals(input)) {
            return new WnIntegerFaker();
        }

        // 生成一个随机颜色
        // "HEXS:2R0:#{0-FF}{0-FF}{0-FF}",
        if (input.startsWith("HEXS:")) {
            input = input.substring(5).trim();
            return new WnHexTmplFaker(input, false);
        }
        // 生成 50-100 的整数
        // "HEX:6F-FF",
        if (input.startsWith("HEX:")) {
            input = input.substring(4).trim();
            return new WnHexFaker(input);
        }
        // 生成 0-100 的整数
        // "HEX",
        if ("HEX".equals(input)) {
            return new WnHexFaker();
        }

        // 生成随机的字符串
        // "STR:5-10",
        if (input.startsWith("STR:")) {
            input = input.substring(4).trim();
            return new WnStrFaker(input);
        }
        // 生成随机的字符串
        // "STR",
        else if (input.startsWith("STR")) {
            return new WnStrFaker();
        }
        // 生成随机的句子
        // "SENTENCE:5-10",
        else if (input.startsWith("SENTENCE:")) {
            input = input.substring(9).trim();
            return new WnSentenceFaker(lang, input);
        }
        // 生成随机的句子
        // "SENTENCE",
        if (input.startsWith("SENTENCE")) {
            return new WnSentenceFaker(lang);
        }
        // 生成随机的文本
        // "TEXT:5-10",
        if (input.startsWith("TEXT:")) {
            input = input.substring(5).trim();
            return new WnTextFaker(lang, input);
        }
        // 生成随机的文本
        // "TEXT",
        if (input.startsWith("TEXT")) {
            return new WnTextFaker(lang);
        }
        // 生成随机名称
        // "NAME"
        if (input.startsWith("NAME:")) {
            input = input.substring(5).trim().toUpperCase();
            WnNameFakeMode fm = WnNameFakeMode.valueOf(input);

            return new WnNameFaker(lang, fm);
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
        m = P_AMS.matcher(input);
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

    public static final String TP_NAME_FAM = "name_fam";
    public static final String TP_NAME_FIRST = "name_first";
    public static final String TP_NAME_MID = "name_mid";
    public static final String TP_WORDS = "words";

    private static WnFakes _me = null;

    public static WnFakes me() {
        if (null == _me) {
            _me = new WnFakes();
        }
        return _me;
    }

    private Map<String, Map<String, WnFakeWord>> dicts;
    private Map<String, WnTmpl> namePatterns = new HashMap<>();

    public WnFakes() {
        dicts = new HashMap<>();
        dicts.put("en_us", load("en_us"));
        dicts.put("zh_cn", load("zh_cn"));
    }

    Map<String, WnFakeWord> load(String lang) {
        Map<String, WnFakeWord> re = new HashMap<>();
        String pkg = WnFakes.class.getPackage().getName().replace('.', '/');
        String base = pkg + "/data/" + lang + "/";
        String[] names = Wlang.array(TP_NAME_FAM, TP_NAME_FIRST, TP_NAME_MID, TP_WORDS);
        for (String name : names) {
            String fph = base + name + ".txt";
            String str = Files.read(fph);
            WnFakeWord fw = new WnFakeWord(str);
            re.put(name, fw);
        }
        String fph = base + "name.txt";
        String str = Files.read(fph);
        WnTmpl tmpl = WnTmpl.parse(str);
        namePatterns.put(lang, tmpl);
        return re;
    }

    public WnTmpl getNamePattern(String lang) {
        return namePatterns.get(lang);
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
