package org.nutz.walnut.ext.entity.statistics.bean;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.Times;

public class DayMarkUnit extends MarkUnit {

    @Override
    public List<MarkRange> evalRanges(long beginInMs, long endInMs) {
        // 对齐开头
        Calendar c0 = Calendar.getInstance();
        c0.setTimeInMillis(beginInMs);
        c0.set(Calendar.HOUR_OF_DAY, 0);
        c0.set(Calendar.MINUTE, 0);
        c0.set(Calendar.SECOND, 0);
        c0.set(Calendar.MILLISECOND, 0);

        // 对齐末尾
        Calendar c1 = Calendar.getInstance();
        c1.setTimeInMillis(endInMs + 86400000L);
        c1.set(Calendar.HOUR_OF_DAY, 0);
        c1.set(Calendar.MINUTE, 0);
        c1.set(Calendar.SECOND, 0);
        c1.set(Calendar.MILLISECOND, 0);
        endInMs = c1.getTimeInMillis();

        // c1 - 下一天
        c1.setTime(c0.getTime());
        c1.set(Calendar.DATE, c1.get(Calendar.DATE) + 1);

        // 准备返回列表
        List<MarkRange> list = new LinkedList<>();

        // 循环柴扉
        while (c1.getTimeInMillis() < endInMs) {
            MarkRange mr = new MarkRange();
            mr.setBeginInMs(c0.getTimeInMillis());
            mr.setEndInMs(c1.getTimeInMillis());
            mr.setName(Times.sD(c0.getTime()));
            list.add(mr);

            // 移动
            c0.setTime(c1.getTime());
            c1.set(Calendar.DATE, c1.get(Calendar.DATE) + 1);
        }

        // 搞定
        return list;
    }

}
