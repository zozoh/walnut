package org.nutz.walnut.ext.data.tmpl;

import org.nutz.walnut.impl.box.JvmFilterExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.tmpl.WnTmpl;

public class cmd_tmpl extends JvmFilterExecutor<TmplContext, TmplFilter> {

    public cmd_tmpl() {
        super(TmplContext.class, TmplFilter.class);
    }

    @Override
    protected TmplContext newContext() {
        return new TmplContext();
    }

    @Override
    protected void prepare(WnSystem sys, TmplContext fc) {}

    @Override
    protected void output(WnSystem sys, TmplContext fc) {
        if (!fc.quiet) {
            WnTmpl t = WnTmpl.parse(fc.tmpl);
            String s = t.render(fc.vars);
            sys.out.println(s);
        }
    }
}
