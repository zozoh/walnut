package org.nutz.walnut.ext.dsync;

import org.nutz.walnut.impl.box.JvmFilterExecutor;
import org.nutz.walnut.impl.box.WnSystem;

public class cmd_dsync extends JvmFilterExecutor<DSyncContext, DSyncFilter> {

    public cmd_dsync() {
        super(DSyncContext.class, DSyncFilter.class);
    }

    @Override
    protected DSyncContext newContext() {
        return new DSyncContext();
    }

    @Override
    protected void prepare(WnSystem sys, DSyncContext fc) {
        fc.api = new WnDataSyncService(sys);

        String confName = fc.params.val(0, "dsync");
        fc.config = fc.api.loadConfig(confName);
    }

    @Override
    protected void output(WnSystem sys, DSyncContext fc) {}

}
