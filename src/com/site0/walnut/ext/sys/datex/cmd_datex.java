package com.site0.walnut.ext.sys.datex;

import java.util.Calendar;
import java.util.Date;
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
        if (fc.quiet) {
            return;
        }
        // 输出毫秒数
        if ("AMS".equalsIgnoreCase(fc.fmt)) {
            sys.out.println(fc.now.getTimeInMillis());
        }
        // 格式化日期
        else {
            Date d = fc.now.getTime();
            String str;
            // 指定了时区
            if (null != fc.displayTimeZone) {
                str = Wtime.format(d, fc.fmt, fc.displayTimeZone);
            }
            // 默认会采用当前线程时区（也就是会话时区）
            else {
                str = Wtime.format(d, fc.fmt);
            }
            sys.out.println(str);
        }
    }

}
