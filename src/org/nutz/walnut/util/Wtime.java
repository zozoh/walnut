package org.nutz.walnut.util;

import java.util.Calendar;

public abstract class Wtime {

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
