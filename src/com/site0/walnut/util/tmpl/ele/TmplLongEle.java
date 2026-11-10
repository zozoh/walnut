package com.site0.walnut.util.tmpl.ele;

import org.nutz.castor.Castors;
import com.site0.walnut.util.Ws;

public class TmplLongEle extends TmplDynamicEle {

    public TmplLongEle(String key, String fmt, String dft) {
        super("long", key, fmt, dft);
        this.fmt = Ws.sNull(fmt, "%d");
    }

    @Override
    protected String _val(Object val) {
        Long n = Castors.me().castTo(val, Long.class);
        if (null != n) {
            return String.format(fmt, n);
        }
        return null;
    }

}
