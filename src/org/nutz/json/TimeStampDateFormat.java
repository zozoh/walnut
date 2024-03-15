package org.nutz.json;

import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.site0.walnut.util.Wlang;

/**
 * 仅用于将Date对象变成long
 * @author wendal
 *
 */
class TimeStampDateFormat extends SimpleDateFormat {

    private static final long serialVersionUID = 1L;

    public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
        return toAppendTo.append(""+date.getTime());
    }

    public Date parse(String source, ParsePosition pos) {
        throw Wlang.noImplement();
    }

}
