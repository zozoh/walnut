package com.site0.walnut.ext.sys.lock;

import java.util.LinkedList;
import java.util.List;

import com.site0.walnut.api.lock.WnLock;
import com.site0.walnut.api.lock.WnLockApi;
import com.site0.walnut.impl.box.JvmFilterContext;

public class LockContext extends JvmFilterContext {

    public WnLockApi api;

    public List<WnLock> locks;

    public LockContext() {
        this.locks = new LinkedList<>();
    }
}
