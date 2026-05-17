package com.site0.walnut.ext.sys.lock.hdl;

import com.site0.walnut.ext.sys.lock.LockContext;
import com.site0.walnut.ext.sys.lock.LockFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.login.usr.WnUser;
import com.site0.walnut.util.ZParams;

public class lock_info extends LockFilter {

    @Override
    protected void process(WnSystem sys, LockContext fc, ZParams params) {
        String info = fc.api.getInfo();
        WnUser me = sys.getMe();
        String myName = me.getName();
        String ticket = sys.session.getTicket();
        sys.out.printlnf("Me=%s; se=%s; Lock=%s", myName, ticket, info);
        fc.quiet = true;
    }

}
