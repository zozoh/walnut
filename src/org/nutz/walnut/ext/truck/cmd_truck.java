package org.nutz.walnut.ext.truck;

import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.walnut.impl.box.JvmFilterExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class cmd_truck extends JvmFilterExecutor<TruckContext, TruckFilter> {

    private static final Log log = Logs.get();
    
    public cmd_truck() {
        super(TruckContext.class, TruckFilter.class);
    }
    
    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "cqnbish", "^(quiet|ajax|json)$");
    }

    @Override
    protected TruckContext newContext() {
        return new TruckContext();
    }

    @Override
    protected void prepare(WnSystem sys, TruckContext fc) {}

    @Override
    protected void output(WnSystem sys, TruckContext fc) {}
    
}
