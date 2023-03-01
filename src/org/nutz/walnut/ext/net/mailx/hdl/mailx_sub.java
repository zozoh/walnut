package org.nutz.walnut.ext.net.mailx.hdl;

import org.nutz.lang.Strings;
import org.nutz.walnut.ext.net.mailx.MailxContext;
import org.nutz.walnut.ext.net.mailx.MailxFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class mailx_sub extends MailxFilter {

    @Override
    protected void process(WnSystem sys, MailxContext fc, ZParams params) {
        String subject = Strings.join(" ", params.vals);
        if (!Strings.isBlank(subject)) {
            fc.mail.setSubject(subject);
        }
    }

}
