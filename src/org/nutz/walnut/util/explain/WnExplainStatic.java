package org.nutz.walnut.util.explain;

import org.nutz.lang.util.NutBean;

public class WnExplainStatic implements WnExplain {

    private Object val;

    public WnExplainStatic(Object val) {
        this.val = val;
    }

    @Override
    public Object explain(NutBean context) {
        return val;
    }

}
