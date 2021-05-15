package org.nutz.walnut.ext.data.fake.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.data.fake.WnFaker;
import org.nutz.walnut.util.Ws;

public class WnBeanFaker extends WnWordFaker implements WnFaker<NutBean> {

    private Map<String, WnFaker<?>> tmpl;

    @SuppressWarnings("unchecked")
    public WnBeanFaker(String lang, NutMap map) {
        super(lang);
        tmpl = new LinkedHashMap<>();
        for (Map.Entry<String, Object> en : map.entrySet()) {
            String key = en.getKey();
            Object val = en.getValue();

            // 空值
            if (null == val) {
                tmpl.put(key, new WnStaticFaker(null));
            }
            // 字符串，看看有没有特殊的设定
            else if (val instanceof String) {
                String str = (String) val;
                // 生成 UU32
                // "key1" : "UU32",
                if ("UU32".equals(str)) {
                    tmpl.put(key, new WnUU32Faker());
                }
                // 生成 0-100 的整数
                // "key2" : "INT",
                else if ("INT".equals(str)) {
                    tmpl.put(key, new WnIntegerFaker());
                }
                // 生成 50-100 的整数
                // "key3" : "INT:50-100",
                else if (str.startsWith("INT:")) {
                    str = str.substring(4).trim();
                    tmpl.put(key, new WnIntegerFaker(str));
                }
                // 生成 随机的多段整数
                // "key4" : "INTS:192.168.{100-200}.{0-255}",
                else if (str.startsWith("INTS:")) {
                    str = str.substring(5).trim();
                    tmpl.put(key, new WnIntTmplFaker(str));
                }
                // 生成随机的句子
                // "key5" : "STR",
                else if (str.startsWith("STR")) {
                    tmpl.put(key, new WnStrFaker());
                }
                // 生成随机的句子
                // "key5" : "STR:5-10",
                else if (str.startsWith("STR:")) {
                    str = str.substring(5).trim();
                    int pos = str.indexOf('-');
                    int min = Integer.parseInt(str.substring(0, pos).trim());
                    int max = Integer.parseInt(str.substring(pos + 1).trim());
                    tmpl.put(key, new WnStrFaker(min, max));
                }
                // 生成随机的句子
                // "key5" : "SENTENCE",
                else if (str.startsWith("SENTENCE")) {
                    tmpl.put(key, new WnSentenceFaker(lang));
                }
                // 生成随机的句子
                // "key5" : "SENTENCE:5-10",
                else if (str.startsWith("SENTENCE:")) {
                    str = str.substring(5).trim();
                    int pos = str.indexOf('-');
                    int min = Integer.parseInt(str.substring(0, pos).trim());
                    int max = Integer.parseInt(str.substring(pos + 1).trim());
                    tmpl.put(key, new WnSentenceFaker(lang, min, max));
                }
                // 生成随机的句子
                // "key5" : "TEXT",
                else if (str.startsWith("TEXT")) {
                    tmpl.put(key, new WnTextFaker(lang));
                }
                // 生成随机的句子
                // "key5" : "TEXT:5-10",
                else if (str.startsWith("TEXT:")) {
                    str = str.substring(5).trim();
                    int pos = str.indexOf('-');
                    int min = Integer.parseInt(str.substring(0, pos).trim());
                    int max = Integer.parseInt(str.substring(pos + 1).trim());
                    tmpl.put(key, new WnTextFaker(lang, min, max));
                }
                // 生成随机名称
                // "key6" : "NAME"
                else if ("NAME".equals(str)) {
                    tmpl.put(key, new WnNameFaker(lang));
                }
                // 日期时间
                else if (str.startsWith("AMS")) {
                    WnFaker<?> ams = null;
                    String[] ss = Ws.splitIgnoreBlank(str, ":");
                    if (ss.length == 1) {
                        ams = new WnAmsFaker("today", "5m", true);
                    }
                    // 指定了日期
                    else if (ss.length == 2) {
                        ams = new WnAmsFaker(ss[1], "5m", true);
                    }
                    // 指定了日期和范围
                    else if (ss.length == 3) {
                        ams = new WnAmsFaker(ss[1], ss[2], true);
                    }
                    // 指定了日期范围和格式
                    else {
                        WnAmsFaker faker = new WnAmsFaker(ss[1], ss[2], true);
                        String fmt = Ws.join(ss, ":", 3, ss.length - 3);
                        ams = new WnFormatAmsFaker(faker, fmt);
                    }
                    tmpl.put(key, ams);
                }
                // 那就是静态字符串了
                else {
                    tmpl.put(key, new WnStaticFaker(val));
                }
            }
            // 内置的 Bean
            else if (val instanceof Map<?, ?>) {
                NutMap vmap = NutMap.WRAP((Map<String, Object>) val);
                WnBeanFaker bf = new WnBeanFaker(lang, vmap);
                tmpl.put(key, bf);
            }
            // 那就是静态的值
            else {
                tmpl.put(key, new WnStaticFaker(val));
            }
        }
    }

    @Override
    public NutBean next() {
        NutMap bean = new NutMap();
        for (Map.Entry<String, WnFaker<?>> en : tmpl.entrySet()) {
            String key = en.getKey();
            WnFaker<?> faker = en.getValue();
            Object val = faker.next();
            bean.put(key, val);
        }
        return bean;
    }

}
