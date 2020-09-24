package org.nutz.walnut.ext.hookx;

import org.nutz.walnut.impl.box.JvmFilterExecutor;
import org.nutz.walnut.impl.box.WnSystem;

public class cmd_hookx extends JvmFilterExecutor<HookXContext, HookXFilter> {

    public cmd_hookx() {
        super(HookXContext.class, HookXFilter.class);
    }

    @Override
    protected HookXContext newContext() {
        return new HookXContext();
    }

    @Override
    protected void prepare(WnSystem sys, HookXContext fc) {}

    @Override
    protected void output(WnSystem sys, HookXContext fc) {}

}
