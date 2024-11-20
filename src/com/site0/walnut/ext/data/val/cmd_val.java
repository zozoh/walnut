package com.site0.walnut.ext.data.val;

import org.nutz.lang.util.NutMap;

import com.site0.walnut.impl.box.JvmFilterExecutor;
import com.site0.walnut.impl.box.WnSystem;

public class cmd_val extends JvmFilterExecutor<ValContext, ValFilter> {

    public cmd_val() {
        super(ValContext.class, ValFilter.class);
    }

    @Override
    protected ValContext newContext() {
        return new ValContext();
    }

    @Override
    protected void prepare(WnSystem sys, ValContext fc) {
        fc.context = new NutMap();

    }

    @Override
    protected void output(WnSystem sys, ValContext fc) {
        if (null != fc.result) {
            sys.out.print(fc.result.toString());
        }
    }

}
