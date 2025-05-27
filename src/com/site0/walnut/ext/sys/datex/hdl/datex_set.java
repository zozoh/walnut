package com.site0.walnut.ext.sys.datex.hdl;

import java.util.Date;
import java.util.TimeZone;

import com.site0.walnut.ext.sys.datex.DatexContext;
import com.site0.walnut.ext.sys.datex.DatexFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.Wtime;
import com.site0.walnut.util.ZParams;

public class datex_set extends DatexFilter {

    @Override
    protected void process(WnSystem sys, DatexContext fc, ZParams params) {
        String input = params.val_check(0);

        // 设置时区
        TimeZone tz = null;
        if (params.has("tz")) {
            String tzId = params.getString("tz");
            tz = TimeZone.getTimeZone(tzId);
        }

        // 防空
        if (Ws.isBlank(input)) {
            return;
        }

        // 尝试直接设置时间
        try {
            Date d = Wtime.parseAnyDate(input, tz);
            fc.now.setTime(d);
        }
        catch (RuntimeException e) {
            // 尝试采用时间宏
            long ams = Wtime.valueOf(input);
            Date d = new Date(ams);
            fc.now.setTime(d);
        }
    }

}
