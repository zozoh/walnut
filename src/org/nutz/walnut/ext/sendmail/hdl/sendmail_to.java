package org.nutz.walnut.ext.sendmail.hdl;

import org.nutz.walnut.ext.sendmail.SendmailContext;
import org.nutz.walnut.ext.sendmail.SendmailFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class sendmail_to extends SendmailFilter {

    @Override
    protected void process(WnSystem sys, SendmailContext fc, ZParams params) {
        fc.mail.addReceivers(params.vals);
    }

}
