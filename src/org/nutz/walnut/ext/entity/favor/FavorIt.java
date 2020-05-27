package org.nutz.walnut.ext.entity.favor;

import java.util.Date;

import org.nutz.lang.Times;

public class FavorIt {

    private String target;

    private long time;

    public FavorIt() {}

    public FavorIt(String target, long time) {
        this.setTarget(target);
        this.setTime(time);
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getTimeText() {
        Date d = Times.D(time);
        return Times.format("yyyy-MM-dd HH:mm:ss", d);
    }

    public String toString() {
        return String.format("%s:%d", target, time);
    }

}
