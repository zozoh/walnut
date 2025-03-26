package com.site0.walnut.ext.net.webx.hdl;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.ext.net.webx.WebxContext;
import com.site0.walnut.ext.net.webx.WebxFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

public class webx_logout extends WebxFilter {

    @Override
    protected void process(WnSystem sys, WebxContext fc, ZParams params) {
        String ticket = params.val_check(0);

        try {
            fc.result = fc.api.logout(ticket);
        }
        catch (Throwable e) {
            fc.error = Er.wrap(e);
        }

    }

}
