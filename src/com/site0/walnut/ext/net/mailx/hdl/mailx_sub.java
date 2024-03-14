package com.site0.walnut.ext.net.mailx.hdl;

import org.nutz.lang.Strings;
import com.site0.walnut.ext.net.mailx.MailxContext;
import com.site0.walnut.ext.net.mailx.MailxFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

public class mailx_sub extends MailxFilter {

    @Override
    protected void process(WnSystem sys, MailxContext fc, ZParams params) {
        String subject = Strings.join(" ", params.vals);
        if (!Strings.isBlank(subject)) {
            fc.mail.setSubject(subject);
        }
    }

}
