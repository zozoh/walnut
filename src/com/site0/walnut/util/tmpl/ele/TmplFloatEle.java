package com.site0.walnut.util.tmpl.ele;

import java.text.DecimalFormat;

import org.nutz.castor.Castors;
import org.nutz.lang.Strings;

public class TmplFloatEle extends TmplDynamicEle {

    private DecimalFormat decFormat;

    public TmplFloatEle(String key, String fmt, String dft) {
        super("float", key, fmt, dft);
        // this.fmt = Strings.sNull(fmt, "%#.2f");
        this.fmt = Strings.sNull(fmt, "0.##");
        if (this.fmt.indexOf('%') >= 0) {
            // 采用普通的格式化，那么就不需要 decFormat
        } else {
            this.decFormat = new DecimalFormat(this.fmt);
        }
    }

    @Override
    protected String _val(Object val) {
        Float n = Castors.me().castTo(val, Float.class);
        if (null != n) {
            if (null != this.decFormat) {
                return this.decFormat.format(n);
            }
            return String.format(this.fmt, n);
        }
        return null;
    }

}
