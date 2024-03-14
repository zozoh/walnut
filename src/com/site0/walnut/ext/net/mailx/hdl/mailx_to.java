package com.site0.walnut.ext.net.mailx.hdl;

import com.site0.walnut.ext.net.mailx.MailxContext;
import com.site0.walnut.ext.net.mailx.MailxFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

public class mailx_to extends MailxFilter {

    @Override
    protected void process(WnSystem sys, MailxContext fc, ZParams params) {
        fc.mail.addTo(params.vals);
    }

}
