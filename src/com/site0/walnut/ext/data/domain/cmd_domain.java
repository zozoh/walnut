package com.site0.walnut.ext.data.domain;

import com.site0.walnut.impl.box.JvmFilterExecutor;
import com.site0.walnut.impl.box.WnSystem;

public class cmd_domain extends JvmFilterExecutor<DomainContext, DomainFilter> {

    public cmd_domain() {
        super(DomainContext.class, DomainFilter.class);
    }

    @Override
    protected DomainContext newContext() {
        return new DomainContext();
    }

    @Override
    protected void prepare(WnSystem sys, DomainContext fc) {}

    @Override
    protected void output(WnSystem sys, DomainContext fc) {
        if (fc.quiet) {
            return;
        }
    }

}