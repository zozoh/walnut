package com.site0.walnut.ext.sys.datex.hdl;

import java.util.Calendar;

import com.site0.walnut.ext.sys.datex.DatexContext;
import com.site0.walnut.ext.sys.datex.DatexFilter;
import com.site0.walnut.ext.sys.datex.bean.WnHolidays;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wtime;
import com.site0.walnut.util.ZParams;

public class datex_day extends DatexFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(inmonth|workday)$");
    }

    @Override
    protected void process(WnSystem sys, DatexContext fc, ZParams params) {
        boolean inMonth = params.is("inmonth");
        String mode = params.get("mode", "d");

        // 确保在月内
        int expectY = -1;
        int expectM = -1;
        if (inMonth) {
            expectY = fc.now.get(Calendar.YEAR);
            expectM = fc.now.get(Calendar.MONTH);
        }

        // 绝对
        int day = params.val_int(0, 0);
        if (day > 0) {
            fc.now.set(Calendar.DAY_OF_MONTH, day);
        }

        // 偏移
        int off = params.getInt("i", 0);
        if (off != 0) {
            WnHolidays holi = fc.holidays;
            Wtime.from(fc.now, off, holi, mode);
        }

        if (expectM >= 0) {
            int realY = fc.now.get(Calendar.YEAR);
            int realM = fc.now.get(Calendar.MONTH);
            if (realM != expectM || realY != expectY) {
                if (off > 0) {
                    fc.now.set(expectY, expectM + 1, 0);
                } else {
                    fc.now.set(expectY, expectM, 1);
                }
            }
        }
    }

}
