package org.nutz.walnut.util;

import java.util.Calendar;
import java.util.Date;

public abstract class Wtime {

    public static Calendar today() {
        Calendar c = Calendar.getInstance();
        setDayStart(c);
        return c;
    }

    public static Date todayDate() {
        Calendar c = Calendar.getInstance();
        setDayStart(c);
        return c.getTime();
    }

    public static long todayInMs() {
        Calendar c = Calendar.getInstance();
        setDayStart(c);
        return c.getTimeInMillis();
    }

    /**
     * 将给定的日期对象时间设置为<code>00:00:00.000</code>
     * 
     * @param c
     *            日期
     */
    public static void setDayStart(Calendar c) {
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
    }

}
