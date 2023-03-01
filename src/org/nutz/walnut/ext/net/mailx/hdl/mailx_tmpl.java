package org.nutz.walnut.ext.net.mailx.hdl;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.net.mailx.MailxContext;
import org.nutz.walnut.ext.net.mailx.MailxFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class mailx_tmpl extends MailxFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(html)$");
    }

    @Override
    protected void process(WnSystem sys, MailxContext fc, ZParams params) {
        String tmplName = params.val_check(0);

        WnObj oTmpl = fc.loadTemplateObj(tmplName);

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
