package com.site0.walnut.ext.sys.lock;

import java.util.LinkedList;
import java.util.List;

import com.site0.walnut.api.lock.WnLock;
import com.site0.walnut.api.lock.WnLockApi;
import com.site0.walnut.impl.box.JvmFilterContext;
import com.site0.walnut.util.Ws;

public class LockContext extends JvmFilterContext {

    public WnLockApi api;

    public List<WnLock> locks;

    private boolean globleLock;

    private String domainName;

    private String lockPrefix;
    
    public boolean quiet;

    public LockContext() {
        this.locks = new LinkedList<>();
    }

    public String normalizeLockName(String name) {
        if (null == lockPrefix || globleLock || name.startsWith(lockPrefix)) {
            return name;
        }
        return lockPrefix + name;
    }

    public boolean isGlobleLock() {
        return globleLock;
    }

    public void setGlobleLock(boolean asGlobleLock) {
        this.globleLock = asGlobleLock;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
        if (Ws.isBlank(domainName)) {
            this.lockPrefix = null;
        } else {
            this.lockPrefix = domainName + ":";
        }
    }

}
