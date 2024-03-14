package com.site0.walnut.ext.sys.datex.bean;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import com.site0.walnut.util.Wtime;

public class WnHolidays {

    /**
     * 一周哪些天是周末，默认是 1:SUNDAY, 7:SATURDAY
     */
    private int[] weekend;

    /**
     * 键为 <code>yyyy-MM-dd</code>
     */
    private Map<String, HolidayType> days;

    public WnHolidays() {
        days = new HashMap<>();
        weekend = new int[]{Calendar.SUNDAY, Calendar.SATURDAY};
    }

    public void load(Map<String, Object> map) {
        for (Map.Entry<String, Object> en : map.entrySet()) {
            String key = en.getKey();
            Object val = en.getValue();

            if (null != val && "work".equalsIgnoreCase(val.toString())) {
                days.put(key, HolidayType.WORK);
            } else {
                days.put(key, HolidayType.OFF);
            }
        }
    }

    public HolidayType getDayType(String key) {
        HolidayType t = days.get(key);
        if (null == t) {
            return HolidayType.UNKNOWN;
        }
        return t;
    }

    /**
     * @param c
     *            日期
     * @return 是否是工作日
     */
    public boolean isWordDay(Calendar c) {
        String key = Wtime.format(c.getTime(), "yyyy-MM-dd");
        HolidayType dt = getDayType(key);
        if (HolidayType.OFF == dt) {
            return false;
        }
        if (HolidayType.WORK == dt) {
            return true;
        }
        return !this.isWeekend(c);
    }

    /**
     * @param c
     *            日期
     * @return 是否是休息日
     */
    public boolean isOffDay(Calendar c) {
        String key = Wtime.format(c.getTime(), "yyyy-MM-dd");
        HolidayType dt = getDayType(key);
        if (HolidayType.OFF == dt) {
            return true;
        }
        if (HolidayType.WORK == dt) {
            return false;
        }
        return this.isWeekend(c);
    }

    /**
     * @param c
     *            日期
     * @return 是否是周末
     */
    public boolean isWeekend(Calendar c) {
        if (null == this.weekend || weekend.length == 0) {
            return false;
        }
        int d = c.get(Calendar.DAY_OF_WEEK);
        for (int w : weekend) {
            if (w == d) {
                return true;
            }
        }
        return false;
    }
}
