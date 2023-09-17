package org.nutz.walnut.ext.sys.datex;

import java.util.Calendar;

import org.nutz.walnut.ext.sys.datex.bean.WnHolidays;
import org.nutz.walnut.impl.box.JvmFilterContext;

public class DatexContext extends JvmFilterContext {

    public Calendar now;

    public String fmt;

    public WnHolidays holidays;
}
