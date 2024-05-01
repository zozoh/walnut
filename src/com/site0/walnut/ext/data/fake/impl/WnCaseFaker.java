package com.site0.walnut.ext.data.fake.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.ext.data.fake.WnFaker;
import com.site0.walnut.ext.data.fake.WnFakes;
import com.site0.walnut.util.Wlang;

public abstract class WnCaseFaker implements WnFaker<String> {

    private static final Pattern _P = Pattern.compile("^(UPPER|LOWER|SNAKE|CAMEL|KEBAB):(.+)$",
                                                      Pattern.CASE_INSENSITIVE);

    public static Matcher tryInput(String input) {
        return _P.matcher(input);
    }

    
    public static WnCaseFaker parse(String input, String lang) {
        Matcher m = tryInput(input);
        if (!m.find()) {
            throw Er.create("e.fake.caseFaker.Invalid", input);
        }
        return createFaker(m, lang);
    }


    public static WnCaseFaker createFaker(Matcher m, String lang) {
        String type = m.group(1).toUpperCase();
        String str = m.group(2);
        WnFaker<?> faker = WnFakes.createFaker(str, lang);
        return createFaker(type, faker);
    }

    public static WnCaseFaker createFaker(String type, WnFaker<?> faker) {
        if ("UPPER".equals(type)) {
            return new WnCaseUpperFaker(faker);
        }
        if ("LOWER".equals(type)) {
            return new WnCaseLowerFaker(faker);
        }
        if ("SNAKE".equals(type)) {
            return new WnCaseSnakeFaker(faker);
        }
        if ("CAMEL".equals(type)) {
            return new WnCaseCamelFaker(faker);
        }
        if ("KEBAB".equals(type)) {
            return new WnCaseKebabFaker(faker);
        }
        throw Wlang.impossible();
    }

    private WnFaker<?> faker;

    public WnCaseFaker(WnFaker<?> faker) {
        this.faker = faker;
    }

    protected abstract String toCase(String s);

    @Override
    public String next() {
        Object v = this.faker.next();
        String s = v.toString();
        return toCase(s);
    }

    public WnFaker<?> getFaker() {
        return faker;
    }

    public void setFaker(WnFaker<?> faker) {
        this.faker = faker;
    }

}
