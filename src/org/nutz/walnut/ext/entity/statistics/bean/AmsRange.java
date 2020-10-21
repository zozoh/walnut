package org.nutz.walnut.ext.entity.statistics.bean;

import java.util.Date;

import org.nutz.lang.Times;

public class AmsRange {

    private long beginInMs;

    private long endInMs;

    public AmsRange() {}

    public AmsRange(long beginInMs, long endInMs) {
        this.beginInMs = beginInMs;
        this.endInMs = endInMs;
    }

    public String formatBegin(String dfmt) {
        return Times.format(dfmt, new Date(beginInMs));
    }

    public Date getBeginDate() {
        return new Date(beginInMs);
    }

    public long getBeginInMs() {
        return beginInMs;
    }

    public void setBeginInMs(long beginInMs) {
        this.beginInMs = beginInMs;
    }

    public String formatEnd(String dfmt) {
        return Times.format(dfmt, new Date(endInMs));
    }

    public Date getEndDate() {
        return new Date(endInMs);
    }

    public long getEndInMs() {
        return endInMs;
    }

    public void setEndInMs(long endInMs) {
        this.endInMs = endInMs;
    }

    public boolean isInRange(long ms) {
        return ms >= this.beginInMs && ms < this.endInMs;
    }

    public boolean isInRange(Date d) {
        return isInRange(d.getTime());
    }

    public boolean isInRange(String ds) {
        Date d = Times.D(ds);
        return isInRange(d.getTime());
    }

    public boolean isMakeSense() {
        return beginInMs > 0 && endInMs > beginInMs;
    }

}