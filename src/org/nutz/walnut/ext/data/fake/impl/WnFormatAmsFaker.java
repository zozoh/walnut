package org.nutz.walnut.ext.data.fake.impl;

import java.util.Date;

import org.nutz.walnut.ext.data.fake.WnFaker;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.Wtime;

public class WnFormatAmsFaker implements WnFaker<String> {

    private WnAmsFaker faker;

    private String format;

    public WnFormatAmsFaker(WnAmsFaker faker, String format) {
        this.faker = faker;
        this.format = Ws.sBlank(format, "yyyy-MM-dd HH:mm:ss.SSS");
    }

    @Override
    public String next() {
        long ams = faker.next();
        Date d = new Date(ams);
        return Wtime.format(d, format);
    }

}
