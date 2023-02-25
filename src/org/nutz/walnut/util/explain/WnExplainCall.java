package org.nutz.walnut.util.explain;

import org.nutz.el.El;
import org.nutz.lang.util.Context;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.SimpleContext;
import org.nutz.walnut.util.WnEvalHelper;

public class WnExplainCall implements WnExplain {

    private El el;

    public WnExplainCall(String input) {
        this.el = new El(input);
    }

    @Override
    public Object explain(NutBean context) {
        Context ctx = new SimpleContext(context);
        ctx.set("$wn", WnEvalHelper.me());
        return el.eval(ctx);
    }

}
