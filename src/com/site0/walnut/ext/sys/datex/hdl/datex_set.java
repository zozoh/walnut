package com.site0.walnut.ext.sys.datex.hdl;

import java.util.Date;
import java.util.TimeZone;

import com.site0.walnut.ext.sys.datex.DatexContext;
import com.site0.walnut.ext.sys.datex.DatexFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wtime;
import com.site0.walnut.util.ZParams;

public class datex_set extends DatexFilter {

    @Override
    protected void process(WnSystem sys, DatexContext fc, ZParams params) {
        String input = params.val_check(0);

        TimeZone tz = null;
        if (params.has("tz")) {
            String tzId = params.getString("tz");
            tz = TimeZone.getTimeZone(tzId);
        }

        Date d = Wtime.parseAnyDate(input, tz);
        fc.now.setTime(d);
    }

}
