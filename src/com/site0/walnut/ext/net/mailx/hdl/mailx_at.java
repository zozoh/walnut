package com.site0.walnut.ext.net.mailx.hdl;

import com.site0.walnut.ext.net.mailx.MailxContext;
import com.site0.walnut.ext.net.mailx.MailxFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

public class mailx_at extends MailxFilter {

    @Override
    protected void process(WnSystem sys, MailxContext fc, ZParams params) {
        for (String rph : params.vals) {
            fc.mail.addAttachment(rph);
        }
    }

}
