package org.nutz.walnut.ext.net.sendmail.hdl;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.net.sendmail.SendmailContext;
import org.nutz.walnut.ext.net.sendmail.SendmailFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

public class sendmail_at extends SendmailFilter {

    @Override
    protected void process(WnSystem sys, SendmailContext fc, ZParams params) {
        for (String rph : params.vals) {
            WnObj o = Wn.checkObj(sys, rph);
            fc.mail.addAttachments(o);
        }
    }

}
