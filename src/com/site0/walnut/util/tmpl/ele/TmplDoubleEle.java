package com.site0.walnut.util.tmpl.ele;

import java.text.DecimalFormat;

import org.nutz.castor.Castors;
import org.nutz.lang.Strings;

public class TmplDoubleEle extends TmplDynamicEle {

    private DecimalFormat decFormat;

    public TmplDoubleEle(String key, String fmt, String dft) {
        super("double", key, fmt, dft);
        // this.fmt = Strings.sNull(fmt, "%#.2f");
        this.fmt = Strings.sNull(fmt, "0.######");
        if (this.fmt.indexOf('%') >= 0) {
            // 采用普通的格式化，那么就不需要 decFormat
        } else {
            this.decFormat = new DecimalFormat(this.fmt);
        }
    }

    @Override
    protected String _val(Object val) {
        Double n = Castors.me().castTo(val, Double.class);
        if (null != n) {
            if (null != this.decFormat) {
                return this.decFormat.format(n);
            }
            return String.format(fmt, n);
        }
        return null;
    }

}
