package com.site0.walnut.ext.data.expl;

import com.site0.walnut.impl.box.JvmFilterExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

public class cmd_expl extends JvmFilterExecutor<ExplContext, ExplFilter> {

    public cmd_expl() {
        super(ExplContext.class, ExplFilter.class);
    }

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(showkeys)$");
    }

    @Override
    protected ExplContext newContext() {
        return new ExplContext();
    }

    @Override
    protected void prepare(WnSystem sys, ExplContext fc) {
        fc.showKeys = fc.params.is("showkeys", false);
    }

    @Override
    protected void output(WnSystem sys, ExplContext fc) {
        if (!fc.quiet) {
            String re = fc.renderToStr();
            sys.out.print(re);
        }
    }
}
