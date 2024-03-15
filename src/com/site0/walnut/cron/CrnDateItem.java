package com.site0.walnut.cron;

import java.util.Calendar;

import com.site0.walnut.util.Wlang;

abstract class CrnDateItem extends CrnStdItem {

    public CrnDateItem(CrnStdItem... prevs) {
        super(prevs);
    }

    /**
     * 是否匹配一个日期
     * 
     * @param c
     *            日期
     * @return 是否匹配
     */
    abstract boolean match(Calendar c);

    @Override
    public boolean match(int v, int min, int max) {
        throw Wlang.makeThrow("Please use : match(Calendar c)");
    }

    @Override
    public CrnDateItem setIgnoreAnyWhenPrevAllAny(boolean ignore) {
        return (CrnDateItem) super.setIgnoreAnyWhenPrevAllAny(ignore);
    }

}
