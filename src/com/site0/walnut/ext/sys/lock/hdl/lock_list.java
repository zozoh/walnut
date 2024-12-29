package com.site0.walnut.ext.sys.lock.hdl;

import com.site0.walnut.ext.sys.lock.LockContext;
import com.site0.walnut.ext.sys.lock.LockFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

public class lock_list extends LockFilter {

    @Override
    protected void process(WnSystem sys, LockContext fc, ZParams params) {
        fc.locks = fc.api.list();
    }

}
