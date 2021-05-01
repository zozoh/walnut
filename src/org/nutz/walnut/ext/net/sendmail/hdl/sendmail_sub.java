package org.nutz.walnut.ext.net.sendmail.hdl;

import org.nutz.lang.Strings;
import org.nutz.walnut.ext.net.sendmail.SendmailContext;
import org.nutz.walnut.ext.net.sendmail.SendmailFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class sendmail_sub extends SendmailFilter {

    @Override
    protected void process(WnSystem sys, SendmailContext fc, ZParams params) {
        String subject = Strings.join(" ", params.vals);
        if (!Strings.isBlank(subject)) {
            fc.mail.setSubject(subject);
        }
    }

}
