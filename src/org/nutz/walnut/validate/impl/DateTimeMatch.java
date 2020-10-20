package org.nutz.walnut.validate.impl;

import java.util.Calendar;
import java.util.Date;

import org.nutz.lang.Times;
import org.nutz.lang.util.Region;
import org.nutz.walnut.validate.WnMatch;

public class DateTimeMatch implements WnMatch {

    private Region<Date> region;

    public DateTimeMatch(String input) {
        this.region = Region.Date(input);
    }

    @Override
    public boolean match(Object val) {
        if (null == val)
            return false;

        Date d;

        // 毫秒数
        if (val instanceof Number) {
            long ams = ((Number) val).longValue();
            d = new Date(ams);
        }
        // 字符串
        else if (val instanceof CharSequence) {
            d = Times.D(val.toString());
        }
        // 日期对象
        else if (val instanceof Date) {
            d = (Date) val;
        }
        // 日历对象
        else if (val instanceof Calendar) {
            d = ((Calendar) val).getTime();
        }
        // 其他就不支持了
        else {
            return false;
        }

        return region.match(d);
    }

}
