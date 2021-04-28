package org.nutz.walnut.ext.net.sendmail.hdl;

import org.nutz.walnut.ext.net.sendmail.SendmailContext;
import org.nutz.walnut.ext.net.sendmail.SendmailFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class sendmail_tmpl extends SendmailFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(html)$");
    }

    @Override
    protected void process(WnSystem sys, SendmailContext fc, ZParams params) {
        fc.mail.setTemplateName(params.val_check(0));
        if (params.has("html"))
            fc.mail.setAsHtml(params.is("html"));
    }

}
