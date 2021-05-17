package org.nutz.walnut.ext.data.fake.impl;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.data.fake.WnFaker;
import org.nutz.walnut.ext.data.fake.WnFakes;
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
                WnFaker<?> faker = WnFakes.createFaker(str, lang);
                tmpl.put(key, faker);
            }
            // 一个枚举 Faker
            else if (val instanceof Collection<?>) {
                tmpl.put(key, new WnEnumFaker((Collection<?>) val));
            }
            // 一个枚举 Faker
            else if (val.getClass().isArray()) {
                tmpl.put(key, new WnEnumFaker((Object[]) val));
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
