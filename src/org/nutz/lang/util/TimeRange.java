package org.nutz.lang.util;

import org.nutz.lang.Times;

public class TimeRange extends ValueRange<Integer> {

    public TimeRange() {
        super();
    }

    public TimeRange(String str) {
        super();
        this.valueOf(str);
    }

    public Integer fromString(String str) {
        return Times.T(str);
    }

    public String toString(int sec) {
        Times.TmInfo ti = Times.Ti(sec);
        return ti.toString();
    }

}
