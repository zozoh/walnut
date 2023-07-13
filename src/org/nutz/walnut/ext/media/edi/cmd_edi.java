package org.nutz.walnut.ext.media.edi;

import org.nutz.walnut.impl.box.JvmFilterExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class cmd_edi extends JvmFilterExecutor<EdiContext, EdiFilter> {

    public cmd_edi() {
        super(EdiContext.class, EdiFilter.class);
    }

    @Override
    protected ZParams parseParams(String[] args) {
        return super.parseParams(args);
    }

    @Override
    protected EdiContext newContext() {
        return new EdiContext();
    }

    @Override
    protected void prepare(WnSystem sys, EdiContext fc) {

    }

    @Override
    protected void output(WnSystem sys, EdiContext fc) {}

}
