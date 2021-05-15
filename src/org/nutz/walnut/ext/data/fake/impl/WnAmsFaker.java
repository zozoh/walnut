package org.nutz.walnut.ext.data.fake.impl;

import org.nutz.walnut.ext.data.fake.WnFaker;
import org.nutz.walnut.util.Wtime;
import org.nutz.walnut.util.Wuu;

public class WnAmsFaker implements WnFaker<Long> {

    private long startAms;

    private long duMs;

    private boolean autoIncrease;

    private int index;

    public WnAmsFaker() {
        this(System.currentTimeMillis(), 60000);
    }

    public WnAmsFaker(long startAms, long duMs) {
        this(startAms, duMs, true);
    }

    /**
     * @param start
     *            开始的时间
     * @param du
     *            随机偏移的毫秒区间
     * @param autoIncrease
     *            是否每次模拟输出，都要叠加一个区间
     */
    public WnAmsFaker(String start, String du, boolean autoIncrease) {
        this.startAms = Wtime.valueOf(start);
        this.duMs = Wtime.millisecond(du);
        this.autoIncrease = autoIncrease;
        this.index = 0;
    }

    /**
     * @param startAms
     *            开始的毫秒数
     * @param duMs
     *            随机毫秒区间
     * @param autoIncrease
     *            是否每次模拟输出，都要叠加一个区间
     */
    public WnAmsFaker(long startAms, long duMs, boolean autoIncrease) {
        this.startAms = startAms;
        this.duMs = duMs;
        this.autoIncrease = autoIncrease;
        this.index = 0;
    }

    @Override
    public Long next() {
        // 开始毫秒点
        long ams = this.startAms + (this.duMs * this.index);

        // 随机值
        long ms = Wuu.random(0, duMs);

        // 自动增加
        if (this.autoIncrease) {
            this.index++;
        }
        return ams + ms;
    }

}
