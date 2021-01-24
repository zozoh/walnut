package org.nutz.walnut.ext.dom;

import org.nutz.walnut.impl.box.JvmFilterExecutor;
import org.nutz.walnut.impl.box.WnSystem;

public class cmd_dom extends JvmFilterExecutor<DomContext, DomFilter> {

    public cmd_dom() {
        super(DomContext.class, DomFilter.class);
    }

    @Override
    protected DomContext newContext() {
        return new DomContext();
    }

    @Override
    protected void prepare(WnSystem sys, DomContext fc) {}

    @Override
    protected void output(WnSystem sys, DomContext fc) {
        
    }

}
