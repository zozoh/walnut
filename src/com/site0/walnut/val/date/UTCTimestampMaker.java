package com.site0.walnut.val.date;

import java.util.Date;
import org.nutz.lang.util.NutBean;

import com.site0.walnut.util.Ws;
import com.site0.walnut.util.Wtime;
import com.site0.walnut.val.ValueMaker;

public class UTCTimestampMaker implements ValueMaker {

    private String offset;

    public UTCTimestampMaker(String setup) {
        this.offset = Ws.sBlank(setup, null);
    }

    @Override
    public Object make(Date hint, NutBean context) {
        Date d = hint;
        if (null != offset) {
            long ams = hint.getTime();
            long ms2 = Wtime.applyOffset(ams, offset);
            d = new Date(ms2);
        }
        return d.getTime();
    }
}