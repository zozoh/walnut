package org.nutz.walnut.ext.net.mailx.hdl;

import org.nutz.walnut.ext.net.mailx.MailxContext;
import org.nutz.walnut.ext.net.mailx.MailxFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class mailx_at extends MailxFilter {

    @Override
    protected void process(WnSystem sys, MailxContext fc, ZParams params) {
        for (String rph : params.vals) {
            fc.mail.addAttachment(rph);
        }
    }

}