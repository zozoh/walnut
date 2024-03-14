package com.site0.walnut.util.explain;

import org.nutz.lang.util.NutBean;
import org.nutz.mapl.Mapl;

public class WnExplainGet implements WnExplain {

    private String key;

    private Object dft;

    public WnExplainGet(String key, Object dft) {
        this.key = key;
        this.dft = dft;
    }

    @Override
    public Object explain(NutBean context) {
        Object v = Mapl.cell(context, key);
        if (null == v) {
            return dft;
        }
        return v;
    }

}
