package org.nutz.walnut.ext.sys.datex.hdl;

import java.util.Calendar;
import org.nutz.walnut.ext.sys.datex.DatexContext;
import org.nutz.walnut.ext.sys.datex.DatexFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class datex_time extends DatexFilter {

    @Override
    protected void process(WnSystem sys, DatexContext fc, ZParams params) {
        // 那就是绝对时间
        int inMS = params.val_int(0, 0);
        if (inMS < 0) {
            inMS = Math.max(0, 86400000 + inMS);
        }
        inMS = Math.min(86400000 - 1, inMS);
        int inSec = inMS / 1000;
        int inMin = inSec / 60;
        int inHou = inMin / 60;
        int SSS = inMS - inSec * 1000;
        int sec = inSec - inMin * 60;
        int min = inMin - inHou * 60;
        int hou = inHou;

        fc.now.set(Calendar.HOUR_OF_DAY, hou);
        fc.now.set(Calendar.MINUTE, min);
        fc.now.set(Calendar.SECOND, sec);
        fc.now.set(Calendar.MILLISECOND, SSS);

    }

}
