package com.site0.walnut.ext.sys.datex;

import java.util.Calendar;
import java.util.TimeZone;

import com.site0.walnut.ext.sys.datex.bean.WnHolidays;
import com.site0.walnut.impl.box.JvmFilterContext;

public class DatexContext extends JvmFilterContext {

    public Calendar now;

    public String fmt;

    public WnHolidays holidays;

    public TimeZone displayTimeZone;
    
    public boolean quiet;
}
