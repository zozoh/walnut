package com.site0.walnut.ext.net.mailx.hdl;

import org.nutz.lang.Strings;
import com.site0.walnut.ext.net.mailx.MailxContext;
import com.site0.walnut.ext.net.mailx.MailxFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

public class mailx_content extends MailxFilter {

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
