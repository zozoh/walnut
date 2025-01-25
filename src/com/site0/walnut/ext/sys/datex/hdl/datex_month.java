package com.site0.walnut.ext.sys.datex.hdl;

import java.util.Calendar;

import com.site0.walnut.ext.sys.datex.DatexContext;
import com.site0.walnut.ext.sys.datex.DatexFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

public class datex_month extends DatexFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(inmonth)$");
    }

    @Override
    protected void process(WnSystem sys, DatexContext fc, ZParams params) {
        int expectY = -1;
        int expectM = -1;

        // 绝对
        int M = params.val_int(0, 0);
        if (M > 0) {
            expectY = fc.now.get(Calendar.YEAR);
            expectM = M - 1;
            fc.now.set(Calendar.MONTH, expectM);
        }

        // 偏移
        int off = params.getInt("i", 0);
        if (off != 0) {
            expectY = fc.now.get(Calendar.YEAR);
            expectM = fc.now.get(Calendar.MONTH) + off;
            int day = fc.now.get(Calendar.DAY_OF_MONTH);
            fc.now.set(expectY, expectM, day);
        }
    }

}
