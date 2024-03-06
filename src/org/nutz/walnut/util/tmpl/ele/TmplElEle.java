package org.nutz.walnut.util.tmpl.ele;

import org.nutz.el.El;
import org.nutz.lang.util.SimpleContext;
import org.nutz.walnut.util.tmpl.WnTmplRenderContext;

public class TmplElEle extends TmplDynamicEle {

    private El el;

    public TmplElEle(String input) {
        this.key = input;
        this.el = new El(input);
    }

    @Override
    protected String _val(Object val) {
        return null;
    }

    @Override
    public void join(WnTmplRenderContext rc) {
        SimpleContext ctx = new SimpleContext();
        ctx.putAll(rc.context);
        Object obj = el.eval(ctx);
        if (null != obj) {
            rc.sb.append(obj);
        }
    }

    @Override
    public String toString() {
        return "=" + key;
    }

}
