package com.site0.walnut.ext.sys.datex;

import java.util.Calendar;
import java.util.TimeZone;

import com.site0.walnut.ext.sys.datex.bean.WnHolidays;
import com.site0.walnut.impl.box.JvmFilterExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.WnContext;
import com.site0.walnut.util.Wtime;

public class cmd_datex extends JvmFilterExecutor<DatexContext, DatexFilter> {

    public cmd_datex() {
        super(DatexContext.class, DatexFilter.class);
    }

    @Override
    protected DatexContext newContext() {
        return new DatexContext();
    }

    @Override
    protected void prepare(WnSystem sys, DatexContext fc) {
        // 采用域时区
        WnContext wc = Wn.WC();
        TimeZone tz = wc.getTimeZone();
        if (null != tz) {
            fc.now = Calendar.getInstance(tz);
        }
        // 采用系统默认时区
        else {
            fc.now = Calendar.getInstance();
        }
        fc.fmt = "yyyy-MM-dd HH:mm:ss";
        fc.holidays = new WnHolidays();
    }

    @Override
    protected void output(WnSystem sys, DatexContext fc) {
        // 输出毫秒数
        if ("AMS".equalsIgnoreCase(fc.fmt)) {
            sys.out.println(fc.now.getTimeInMillis());
        }
        // 格式化日期
        else {
            sys.out.println(Wtime.format(fc.now.getTime(), fc.fmt));
        }
    }

}
