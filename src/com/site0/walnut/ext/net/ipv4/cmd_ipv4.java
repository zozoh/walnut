package com.site0.walnut.ext.net.ipv4;

import com.site0.walnut.impl.box.JvmFilterExecutor;
import com.site0.walnut.impl.box.WnSystem;

public class cmd_ipv4 extends JvmFilterExecutor<Ipv4Context, Ipv4Filter> {

    public cmd_ipv4() {
        super(Ipv4Context.class, Ipv4Filter.class);
    }

    @Override
    protected Ipv4Context newContext() {
        return new Ipv4Context();
    }

    @Override
    protected void prepare(WnSystem sys, Ipv4Context fc) {

    }

    @Override
    protected void output(WnSystem sys, Ipv4Context fc) {
        sys.out.print(fc.ip);
    }

}
