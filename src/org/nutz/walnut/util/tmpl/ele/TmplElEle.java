package org.nutz.walnut.util.tmpl.ele;

import org.nutz.el.El;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.SimpleContext;

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
    public void join(StringBuilder sb, NutBean context, boolean showKey) {
        SimpleContext ctx = new SimpleContext();
        ctx.putAll(context);
        Object obj = el.eval(ctx);
        if (null != obj) {
            sb.append(obj);
        }
    }

    @Override
    public String toString() {
        return "=" + key;
    }

}
