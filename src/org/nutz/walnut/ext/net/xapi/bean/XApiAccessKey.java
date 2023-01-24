package org.nutz.walnut.ext.net.xapi.bean;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.util.Wtime;

public class XApiAccessKey {

    /**
     * 密钥内容，即所谓的<b>票据</b>
     */
    private String ticket;

    /**
     * 还有多久过期
     */
    private int expiTime;

    /**
     * 过期时间单位，<code>s|m|h</code>
     */
    private String expiTimeUnit;

    /**
     * 过期时间的绝对毫秒数
     */
    private long expiAtMs;

    public NutBean toBean() {
        return Lang.obj2map(this, NutMap.class);
    }

    public boolean hasTicket() {
        return !Strings.isBlank(ticket);
    }

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public boolean isExpired() {
        return expiAtMs > 0 && expiAtMs < System.currentTimeMillis();
    }

    public int getExpiTime() {
        return expiTime;
    }

    public void setExpiTime(int expi) {
        this.expiTime = expi;
    }

    public String getExpiTimeUnit() {
        return expiTimeUnit;
    }

    public void setExpiTimeUnit(String expiTimeUnit) {
        this.expiTimeUnit = expiTimeUnit;
    }

    public void setNowInMs(long nowInMs) {
        if (this.expiTime > 0) {
            long ms = Wtime.millisecond(expiTime + expiTimeUnit);
            this.expiAtMs = ms + nowInMs;
        }
        // 那么就设置成 0 表示永不过期咯
        else {
            this.expiAtMs = 0;
        }
    }

    public long getExpiAtMs() {
        return expiAtMs;
    }

    public void setExpiAtMs(long expiInMs) {
        this.expiAtMs = expiInMs;
    }

}
