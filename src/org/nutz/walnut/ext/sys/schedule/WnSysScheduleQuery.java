package org.nutz.walnut.ext.sys.schedule;

import java.util.Calendar;
import java.util.Date;

import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.Wtime;

public class WnSysScheduleQuery {

    private Date today;

    private String slotRange;

    private String userName;

    /**
     * 跳过多少记录
     */
    private int skip;

    /**
     * 最多列出多少记录
     */
    private int limit;

    public void joinQuery(WnQuery q) {
        // 将时间设置到 00:00:00
        Calendar c = Calendar.getInstance();
        if (null != today) {
            c.setTime(today);
        }
        Wtime.setDayStart(c);

        q.setv("date", c.getTimeInMillis());
        // 指定用户名称
        if (null != userName) {
            q.setv("user", userName);
        }
        // 指定时间槽下标
        if (null != slotRange) {
            q.setv("slot", slotRange);
        }
        if (skip > 0) {
            q.skip(skip);
        }
        if (limit > 0) {
            q.limit(limit);
        }
    }

    public Date getToday() {
        return today;
    }

    public void setToday(Date today) {
        this.today = today;
    }

    public void setToday(long ams) {
        this.today = new Date(ams);
    }

    public void setToday(String today) {
        long ams = Wn.evalDatetimeStrToAMS(today);
        this.today = new Date(ams);
    }

    public String getSlotRange() {
        return slotRange;
    }

    public void setSlotRange(String slotRange) {
        this.slotRange = slotRange;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String user) {
        this.userName = user;
    }

}
