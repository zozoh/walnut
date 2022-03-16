package org.nutz.walnut.ext.data.wf;

import org.nutz.walnut.impl.box.JvmFilterExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class cmd_wf extends JvmFilterExecutor<WfContext, WfFilter> {

    public cmd_wf() {
        super(WfContext.class, WfFilter.class);
    }

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "cqnl");
    }

    @Override
    protected WfContext newContext() {
        return new WfContext();
    }

    @Override
    protected void prepare(WnSystem sys, WfContext fc) {
        String ph = fc.params.val_check(0);
        fc.loadWorkflow(ph);
    }

    @Override
    protected void output(WnSystem sys, WfContext fc) {}
}
