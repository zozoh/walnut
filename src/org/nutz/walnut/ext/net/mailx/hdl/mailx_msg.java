package org.nutz.walnut.ext.net.mailx.hdl;

import org.nutz.lang.Strings;
import org.nutz.walnut.ext.net.mailx.MailxContext;
import org.nutz.walnut.ext.net.mailx.MailxFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class mailx_msg extends MailxFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(html)$");
    }

    @Override
    protected void process(WnSystem sys, MailxContext fc, ZParams params) {
        String msg = Strings.join(" ", params.vals);
        fc.mail.setContent(msg);
        if (params.has("html"))
            fc.mail.setAsHtml(params.is("html"));
    }

}
