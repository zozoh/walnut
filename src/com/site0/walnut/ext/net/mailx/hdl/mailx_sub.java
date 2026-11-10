package com.site0.walnut.ext.net.mailx.hdl;

import com.site0.walnut.util.Ws;
import com.site0.walnut.ext.net.mailx.MailxContext;
import com.site0.walnut.ext.net.mailx.MailxFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

public class mailx_sub extends MailxFilter {

    @Override
    protected void process(WnSystem sys, MailxContext fc, ZParams params) {
        String subject = Ws.join(" ", params.vals);
        if (!Ws.isBlank(subject)) {
            fc.mail.setSubject(subject);
        }
    }

}
