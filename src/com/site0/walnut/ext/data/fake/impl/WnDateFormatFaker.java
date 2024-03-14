package com.site0.walnut.ext.data.fake.impl;

import java.util.Date;

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

    @Override
    public String next() {
        long ams = faker.next();
        Date d = new Date(ams);
        return Wtime.format(d, format);
    }

}
