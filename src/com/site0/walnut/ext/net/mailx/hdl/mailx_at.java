package com.site0.walnut.ext.net.mailx.hdl;

import java.io.IOException;
import java.io.InputStream;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.ext.net.mailx.MailxContext;
import com.site0.walnut.ext.net.mailx.MailxFilter;
import com.site0.walnut.ext.net.mailx.bean.WnMailAttachment;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

public class mailx_at extends MailxFilter {

    @Override
    protected void process(WnSystem sys, MailxContext fc, ZParams params) {
        for (String rph : params.vals) {
            fc.mail.addAttachmentPath(rph);
        }

        // 从标准输入加载附件
        String atName = params.getString("name");
        if (!Ws.isBlank(atName)) {
            String atMime = params.getString("mime", "text/plain");
            InputStream ins = sys.in.getInputStream();
            try {
                byte[] content = ins.readAllBytes();
                fc.mail.addAttachment(new WnMailAttachment(atName, atMime, content));
            }
            catch (IOException e) {
                throw Er.wrap(e);
            }

        }
    }

}
