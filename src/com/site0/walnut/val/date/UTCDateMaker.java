package com.site0.walnut.val.date;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.lang.util.NutBean;

import com.site0.walnut.util.Ws;
import com.site0.walnut.util.Wtime;
import com.site0.walnut.val.ValueMaker;

public class UTCDateMaker implements ValueMaker {

    private String format;

    private String offset;

    public UTCDateMaker(String setup) {
        this.format = "yyyy-MM-dd HH:mm:ss.SSS";
        this.offset = null;
        if (!Ws.isBlank(setup)) {
            Pattern p = Pattern.compile("^(([+-]?[0-9]+[smhdwMy]?)(:)?)?(.*)$");
            Matcher m = p.matcher(setup);
            if (m.find()) {
                this.format = Ws.sBlank(m.group(4), this.format);
                this.offset = Ws.sBlank(m.group(2), null);
            }
        }
    }

    @Override
    public Object make(Date hint, NutBean context) {
        Date d = hint;
        if (null != offset) {
            long ams = hint.getTime();
            long ms2 = Wtime.applyOffset(ams, offset);
            d = new Date(ms2);
        }
        return Wtime.formatUTC(d, format);
    }

}
