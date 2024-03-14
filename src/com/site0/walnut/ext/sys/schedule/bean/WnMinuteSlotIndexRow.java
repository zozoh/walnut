package com.site0.walnut.ext.sys.schedule.bean;

import com.site0.walnut.util.Ws;

public class WnMinuteSlotIndexRow implements Comparable<WnMinuteSlotIndexRow> {

    int index;

    String time;

    int taskCount;

    public WnMinuteSlotIndexRow() {}

    public WnMinuteSlotIndexRow(int index, String time, int taskCount) {
        this.index = index;
        this.time = time;
        this.taskCount = taskCount;
    }

    public WnMinuteSlotIndexRow(String input) {
        this.fromString(input);
    }

    public String toString() {
        return String.format("%04d>%s>%d", index, time, taskCount);
    }

    public void fromString(String input) {
        String[] ss = Ws.splitIgnoreBlank(input, ">");
        if (null != ss && ss.length >= 3) {
            index = Integer.parseInt(ss[0]);
            time = ss[1];
            taskCount = Integer.parseInt(ss[2]);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WnMinuteSlotIndexRow) {
            return this.index == ((WnMinuteSlotIndexRow) obj).index;
        }
        return false;
    }

    @Override
    public int compareTo(WnMinuteSlotIndexRow row) {
        return this.index - row.index;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getTaskCount() {
        return taskCount;
    }

    public void setTaskCount(int taskCount) {
        this.taskCount = taskCount;
    }

}
