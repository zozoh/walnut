package org.nutz.walnut.ext.net.mailx.hdl;

import org.nutz.json.Json;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.net.mailx.MailxContext;
import org.nutz.walnut.ext.net.mailx.MailxFilter;
import org.nutz.walnut.ext.net.mailx.bean.WnMail;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

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
        fc.mail = Json.fromJson(WnMail.class, json);

    }

}
