package com.site0.walnut.ext.net.mailx.hdl;

import java.util.Date;

import org.nutz.lang.util.NutMap;

import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.net.mailx.MailxContext;
import com.site0.walnut.ext.net.mailx.MailxFilter;
import com.site0.walnut.ext.net.mailx.impl.WnMailRawRecieving;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.ZParams;

public class mailx_raw_msg extends MailxFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "cqn", "^(decrypt|header)$");
    }

    @Override
    protected void process(WnSystem sys, MailxContext fc, ZParams params) {
        // 禁止发送
        fc.setQuiet(true);

        // 准备参数
        boolean showHeader = params.is("header");
        boolean isAutoDecrypt = params.is("decrypt");
        String asContent = params.getString("content");
        NutMap fixedMeta = params.getMap("meta");

        sys.out.printlnf("Parse %d raw MimeMessage", params.vals.length);

        // 逐个解析消息
        for (int i = 0; i < params.vals.length; i++) {
            String mimeObjPath = params.val(i);
            sys.out.printlnf("%d) %s", i + 1, mimeObjPath);

            String aph = Wn.normalizeFullPath(mimeObjPath, sys);
            sys.out.printlnf("    > path: %s", aph);

            WnObj obj = sys.io.check(null, aph);
            sys.out.printlnf("    > fobj: %s, len=%d, sha1=%s",
                             obj,
                             obj.len(),
                             obj.sha1());

            String mimeText = sys.io.readText(obj);

            sys.out.println(" > parse MimeMail ...");
            // WnMimeMail mail = new WnMimeMail();
            // mail.fromMessage(mimeText, asContent);

            WnMailRawRecieving rv = new WnMailRawRecieving();
            rv.isAutoDecrypt = isAutoDecrypt;
            rv.mimeText = mimeText;
            rv.recieveDate = new Date(obj.lastModified());
            rv.sys = sys;
            rv.fc = fc;
            rv.asContent = asContent;
            rv.showHeader = showHeader;
            rv.debug = true;
            rv.i = i;
            rv.N = params.vals.length;
            rv.fixedMeta = fixedMeta;

            // 后续处理
            // rv.taTmpl = taTmpl;
            // rv.attachmentTmpl = attachmentTmpl;
            // rv.after = after;
            // rv.outputs = outputs;

            // 执行解析，由于 debug 为 true，会强制输出日志
            rv.run();

        }
    }

}
