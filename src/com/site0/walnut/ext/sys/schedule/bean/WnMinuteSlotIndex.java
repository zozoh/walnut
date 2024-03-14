package com.site0.walnut.ext.sys.schedule.bean;

import java.util.Date;

import org.nutz.lang.Times;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.Wtime;
import com.site0.walnut.util.time.WnDayTime;

/**
 * 为了简便起见，固定了一分钟一个槽，一天就是 1440 个时间槽。
 * <p>
 * 本类封装了对这种索引解析渲染等操作
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnMinuteSlotIndex {

    private Date today;

    private WnMinuteSlotIndexRow[] rows;

    public WnMinuteSlotIndex(Date today) {
        this.today = today;
        rows = new WnMinuteSlotIndexRow[1440];
    }

    public WnMinuteSlotIndex(Date today, String input) {
        this(today);
        this.fromString(input);
    }

    public boolean hasSlot(int index) {
        if (index < 0 || index >= 1440) {
            return false;
        }
        return null != rows[index];
    }

    public void removeSlot(int index) {
        if (index >= 0 && index < rows.length) {
            rows[index] = null;
        }
    }

    public void setSlot(WnMinuteSlotIndexRow row) {
        if (row.index >= 0 && row.index < rows.length) {
            rows[row.index] = row;
            row.time = new WnDayTime(row.index * 60).toString();
        }
    }

    public void setSlot(int index, int taskCount) {
        this.setSlot(new WnMinuteSlotIndexRow(index, null, taskCount));
    }

    public void fromString(String input) {
        String[] lines = Ws.splitIgnoreBlank(input, "\r?\n");
        for (String line : lines) {
            // 无视空行
            if (Ws.isBlank(line)) {
                continue;
            }
            // 注释行
            else if (line.startsWith("#")) {
                // 尝试解析一下日期
                String s = Ws.trim(line.substring(1));
                if (s.startsWith("@[") && s.endsWith("]")) {
                    s = Ws.trim(s.substring(2, s.length() - 1));
                    try {
                        this.today = Wtime.parseDate(s);
                    }
                    catch (Exception e) {}
                }
                // 嗯，普通注释行，可以无视了
                else {
                    continue;
                }
            }
            // 解析分钟槽数据行
            WnMinuteSlotIndexRow row = new WnMinuteSlotIndexRow(line);
            this.setSlot(row);
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (null != today) {
            String ds = Times.format("yyyy-MM-dd", today);
            sb.append("# @[").append(ds).append("]\n");
        }
        for (WnMinuteSlotIndexRow row : rows) {
            if (null != row) {
                sb.append(row).append('\n');
            }
        }
        return sb.toString();
    }

    public Date getToday() {
        return today;
    }

}
