package org.nutz.walnut.ext.entity.statistics.bean;

public class MarkRange {

    private long beginInMs;

    private long endInMs;

    private String name;

    public long getBeginInMs() {
        return beginInMs;
    }

    public void setBeginInMs(long beginInMs) {
        this.beginInMs = beginInMs;
    }

    public long getEndInMs() {
        return endInMs;
    }

    public void setEndInMs(long endInMs) {
        this.endInMs = endInMs;
    }

    public String getName() {
        return name;
    }

    public void setName(String title) {
        this.name = title;
    }

}
