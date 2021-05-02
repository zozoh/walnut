package org.nutz.walnut.ext.sys.schedule;

import java.util.Calendar;
import java.util.Date;

import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.Wtime;
import org.nutz.walnut.util.ZParams;
import org.nutz.walnut.util.time.WnDayTime;

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

    private int slotN;

    public WnSysScheduleQuery() {
        this(1440);
    }

    private WnSysScheduleQuery(int slotN) {
        this.slotN = slotN;
    }

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
            // 仅仅是一个时间槽
            if (slotRange.matches("^(\\d+)$")) {
                int slotIndex = Integer.parseInt(slotRange);
                q.setv("slot", slotIndex);
            }
            // 看起来是一个绝对时间
            else if (slotRange.indexOf(':') > 0) {
                WnDayTime time = new WnDayTime(slotRange);
                int slotIndex = cmd_schedule.timeSlotIndexBySec(time, slotN);
                q.setv("slot", slotIndex);
            }
            // 看起来是一个相对时间
            else if (slotRange.startsWith("now")) {
                long ams = Wn.evalDatetimeStrToAMS(slotRange);
                WnDayTime time = new WnDayTime(ams);
                int slotIndex = cmd_schedule.timeSlotIndexBySec(time, slotN);
                q.setv("slot", slotIndex);
            }
            // 那就一定是一个范围咯，直接设置
            else {
                q.setv("slot", slotRange);
            }
        }
        if (skip > 0) {
            q.skip(skip);
        }
        if (limit > 0) {
            q.limit(limit);
        }
    }

    public void loadFromParams(ZParams params) {
        // 第一个参数指定了日期
        if (params.vals.length > 0) {
            this.setToday(params.val(0));
        }
        // 默认采用今天
        else {
            this.setToday("today");
        }
        // 第二个参数指定了时间槽
        if (params.vals.length > 1) {
            this.setSlotRange(params.val(1));
        }
        // 处理用户信息
        if (params.has("u")) {
            this.userName = params.getString("u", null);
        }
        // 处理游标限制
        this.skip = params.getInt("skip", 0);
        this.limit = params.getInt("limit", 0);
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

    public int getSkip() {
        return skip;
    }

    public int getLimit() {
        return limit;
    }

    public int getSlotN() {
        return slotN;
    }

}
