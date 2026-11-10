package com.site0.walnut.util.tmpl.ele;

import java.util.Date;

import org.nutz.castor.Castors;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.Wtimes;

public class TmplDateEle extends TmplDynamicEle {

    public TmplDateEle(String key, String fmt, String dft) {
        super("date", key, fmt, dft);
        this.fmt = Ws.sNull(fmt, "yyyy-MM-dd'T'HH:mm:ss");
    }

    @Override
    protected String _val(Object val) {
        Date d = Castors.me().castTo(val, Date.class);
        if (null != d)
            return Wtimes.format(fmt, d);
        return null == val ? null : val.toString();
    }

}
