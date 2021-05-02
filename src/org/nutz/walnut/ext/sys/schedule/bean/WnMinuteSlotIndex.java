package org.nutz.walnut.ext.sys.schedule.bean;

import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.time.WnDayTime;

/**
 * 为了简便起见，固定了一分钟一个槽，一天就是 1440 个时间槽。
 * <p>
 * 本类封装了对这种索引解析渲染等操作
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnMinuteSlotIndex {

    private WnMinuteSlotIndexRow[] rows;

    public WnMinuteSlotIndex() {
        rows = new WnMinuteSlotIndexRow[1440];
    }

    public WnMinuteSlotIndex(String input) {
        this();
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
            WnMinuteSlotIndexRow row = new WnMinuteSlotIndexRow(line);
            this.setSlot(row);
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (WnMinuteSlotIndexRow row : rows) {
            if (null != row) {
                sb.append(row).append('\n');
            }
        }
        return sb.toString();
    }

}
