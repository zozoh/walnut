package com.site0.walnut.util.explain;

import org.nutz.lang.util.NutBean;
import org.nutz.mapl.Mapl;
import org.nutz.castor.Castors;

public class WnExplainBoolValue implements WnExplain {

    private String key;

    private Boolean dftBool;

    public WnExplainBoolValue(String key, Object dft) {
        this.key = key;
        this.dftBool = Castors.me().castTo(dft, Boolean.class);
    }

    public WnExplainBoolValue(String key, Object dft, boolean asNot) {
        this.key = key;
        this.dftBool = Castors.me().castTo(dft, Boolean.class);
        this.dftBool = asNot ? (dftBool ? false : true) : dftBool;
    }

    @Override
    public Object explain(NutBean context) {
        Object v = Mapl.cell(context, key);
        if (null == v) {
            return dftBool;
        }
        return Castors.me().castTo(v, Boolean.class);
    }
}
