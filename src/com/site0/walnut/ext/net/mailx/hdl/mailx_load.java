package com.site0.walnut.ext.net.mailx.hdl;

import org.nutz.json.Json;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.net.mailx.MailxContext;
import com.site0.walnut.ext.net.mailx.MailxFilter;
import com.site0.walnut.ext.net.mailx.bean.WnSmtpMail;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.ZParams;

public class mailx_load extends MailxFilter {

    @Override
    protected void process(WnSystem sys, MailxContext fc, ZParams params) {
        String json;
        // 从管道读取
        if (params.vals.length == 0) {
            json = sys.in.readAll();
        }
        // 从文件读取
        else {
            String ph = params.val_check(0);
            WnObj oConf = Wn.checkObj(sys, ph);
            json = sys.io.readText(oConf);
        }
        fc.mail = Json.fromJson(WnSmtpMail.class, json);

    }

}
