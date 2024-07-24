package com.site0.walnut.ext.data.tmpl;

import com.site0.walnut.impl.box.JvmFilterExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;
import com.site0.walnut.util.tmpl.WnTmplX;

public class cmd_tmpl extends JvmFilterExecutor<TmplContext, TmplFilter> {

    public cmd_tmpl() {
        super(TmplContext.class, TmplFilter.class);
    }

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(showkeys)$");
    }

    @Override
    protected TmplContext newContext() {
        return new TmplContext();
    }

    @Override
    protected void prepare(WnSystem sys, TmplContext fc) {
        fc.showKeys = fc.params.is("showkeys", false);
    }

    @Override
    protected void output(WnSystem sys, TmplContext fc) {
        if (!fc.quiet) {
            WnTmplX t = WnTmplX.parse(null, fc.expert, fc.tmpl);
            String s = t.render(fc.vars, fc.showKeys);
            sys.out.println(s);
        }
    }
}
