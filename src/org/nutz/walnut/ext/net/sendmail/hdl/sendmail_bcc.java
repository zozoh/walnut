package org.nutz.walnut.ext.net.sendmail.hdl;

import org.nutz.walnut.ext.net.sendmail.SendmailContext;
import org.nutz.walnut.ext.net.sendmail.SendmailFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class sendmail_bcc extends SendmailFilter {

    @Override
    protected void process(WnSystem sys, SendmailContext fc, ZParams params) {
        fc.mail.addMailBcc(params.vals);
    }

}
