package org.nutz.walnut.util.explain;

import org.nutz.lang.util.NutBean;

public class WnExplainWholeContext implements WnExplain {

    @Override
    public Object explain(NutBean context) {
        return context;
    }

}