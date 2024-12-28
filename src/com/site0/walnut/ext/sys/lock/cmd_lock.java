package com.site0.walnut.ext.sys.lock;

import org.nutz.log.Log;

import com.site0.walnut.impl.box.JvmFilterExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wlog;

public class cmd_lock extends JvmFilterExecutor<LockContext, LockFilter> {
    
    private static final Log log = Wlog.getCMD();

    public cmd_lock() {
        super(LockContext.class, LockFilter.class);
    }

    @Override
    protected LockContext newContext() {
        return new LockContext();
    }

    @Override
    protected void prepare(WnSystem sys, LockContext fc) {}

    @Override
    protected void output(WnSystem sys, LockContext fc) {}

}
