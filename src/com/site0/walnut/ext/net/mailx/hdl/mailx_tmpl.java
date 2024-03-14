package com.site0.walnut.ext.net.mailx.hdl;

import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.net.mailx.MailxContext;
import com.site0.walnut.ext.net.mailx.MailxFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

public class mailx_tmpl extends MailxFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(html)$");
    }

    @Override
    protected void process(WnSystem sys, MailxContext fc, ZParams params) {
        String tmplPath = params.val_check(0);

        WnObj oTmpl = fc.loadContentObj(tmplPath);

        if (null != oTmpl) {
            if (params.has("html")) {
                fc.mail.setAsHtml(true);
            }
            // 根据参数指定
            else {
                fc.mail.setAsHtml(oTmpl.is("mime", "text/html"));
            }
            // 标题
            String subject = fc.mail.getSubject();
            subject = oTmpl.getString("subject", subject);
            fc.mail.setSubject(subject);

            // 内容
            String content = sys.io.readText(oTmpl);
            fc.mail.setContent(content);
        }
    }

}
