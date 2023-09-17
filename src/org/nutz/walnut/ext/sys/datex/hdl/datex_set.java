package org.nutz.walnut.ext.sys.datex.hdl;

import java.util.Date;

import org.nutz.walnut.ext.sys.datex.DatexContext;
import org.nutz.walnut.ext.sys.datex.DatexFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wtime;
import org.nutz.walnut.util.ZParams;

public class datex_set extends DatexFilter {

    @Override
    protected void process(WnSystem sys, DatexContext fc, ZParams params) {
        String input = params.val_check(0);
        Date d = Wtime.parseAnyDate(input);
        fc.now.setTime(d);
    }

}
