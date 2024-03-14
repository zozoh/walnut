package com.site0.walnut.ext.sys.datex.hdl;

import java.util.Date;

import com.site0.walnut.ext.sys.datex.DatexContext;
import com.site0.walnut.ext.sys.datex.DatexFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wtime;
import com.site0.walnut.util.ZParams;

public class datex_set extends DatexFilter {

    @Override
    protected void process(WnSystem sys, DatexContext fc, ZParams params) {
        String input = params.val_check(0);
        Date d = Wtime.parseAnyDate(input);
        fc.now.setTime(d);
    }

}
