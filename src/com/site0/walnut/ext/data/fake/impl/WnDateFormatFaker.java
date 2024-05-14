package com.site0.walnut.ext.data.fake.impl;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.site0.walnut.ext.data.fake.WnFaker;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.Wtime;

public class WnDateFormatFaker implements WnFaker<String> {

    private WnAmsFaker faker;

    private String format;

    public WnDateFormatFaker(WnAmsFaker faker, String format) {
        this.faker = faker;
        this.format = Ws.sBlank(format, "yyyy-MM-dd HH:mm:ss.SSS");
    }

    private static Pattern _P = Pattern.compile("^R([0-9]+)$");

    @Override
    public String next() {
        long ams = faker.next();
        if (Ws.isBlank(format)) {
            return Long.toString(ams);
        }
        // By Radix
        Matcher m = _P.matcher(format);
        if (m.find()) {
            int r = Integer.parseInt(m.group(1));
            return Long.toString(ams, r);
        }
        // 字符串形式
        Date d = new Date(ams);
        return Wtime.format(d, format);
    }

}
