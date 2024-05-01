package com.site0.walnut.ext.data.fake.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.ext.data.fake.WnFaker;
import com.site0.walnut.ext.data.fake.WnFakes;

public class WnSubStrFaker implements WnFaker<String> {

    private static final Pattern _P = Pattern.compile("^SUBSTR([0-9]+):(.+)$",
                                                      Pattern.CASE_INSENSITIVE);

    public static Matcher tryInput(String input) {
        return _P.matcher(input);
    }

    public static WnSubStrFaker parse(String input, String lang) {
        Matcher m = tryInput(input);
        if (!m.find()) {
            throw Er.create("e.fake.caseFaker.Invalid", input);
        }
        return createFaker(m, lang);
    }

    public static WnSubStrFaker createFaker(Matcher m, String lang) {
        int maxLen = Integer.parseInt(m.group(1));
        String str = m.group(2);
        WnFaker<?> faker = WnFakes.createFaker(str, lang);
        return new WnSubStrFaker(faker, maxLen);
    }

    private WnFaker<?> faker;

    private int maxLen;

    public WnSubStrFaker(WnFaker<?> faker, int maxLen) {
        this.faker = faker;
        this.maxLen = maxLen;
    }

    @Override
    public String next() {
        Object v = faker.next();
        String s = v.toString();
        if (s.length() > maxLen) {
            return s.substring(0, maxLen);
        }
        return s;
    }

}
