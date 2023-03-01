package org.nutz.walnut.ext.net.sendmail.hdl;

import org.nutz.lang.Strings;
import org.nutz.walnut.ext.net.sendmail.SendmailContext;
import org.nutz.walnut.ext.net.sendmail.SendmailFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class sendmail_msg extends SendmailFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(html)$");
    }

    @Override
    protected void process(WnSystem sys, SendmailContext fc, ZParams params) {
        String msg = Strings.join(" ", params.vals);
        fc.mail.setContent(msg);
        if (params.has("html"))
            fc.mail.setAsHtml(params.is("html"));
    }

}
