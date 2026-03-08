package org.nutz.validate.impl;

import java.util.Date;

import org.nutz.castor.Castors;
import org.nutz.lang.util.DateRange;
import org.nutz.lang.util.Ranges;
import org.nutz.validate.NutValidateException;
import org.nutz.validate.NutValidator;

public class DateRangeValidator implements NutValidator {

    private DateRange range;

    public DateRangeValidator(String str) {
        this.range = Ranges.Date(str);
    }

    @Override
    public Object check(Object val) throws NutValidateException {
        if (null == val)
            return null;
        Date d = Castors.me().castTo(val, Date.class);
        if (!range.match(d)) {
            throw new NutValidateException("DateOutOfRange", range.toString(), d);
        }
        return val;
    }

    @Override
    public int order() {
        return 102;
    }

}
