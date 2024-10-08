package com.site0.walnut.util.tmpl.ele;

import java.util.Date;

import org.nutz.castor.Castors;
import org.nutz.lang.Strings;
import org.nutz.lang.Times;

public class TmplDateEle extends TmplDynamicEle {

    public TmplDateEle(String key, String fmt, String dft) {
        super("date", key, fmt, dft);
        this.fmt = Strings.sNull(fmt, "yyyy-MM-dd'T'HH:mm:ss");
    }

    @Override
    protected String _val(Object val) {
        Date d = Castors.me().castTo(val, Date.class);
        if (null != d)
            return Times.format(fmt, d);
        return null == val ? null : val.toString();
    }

}
