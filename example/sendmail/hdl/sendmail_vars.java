package org.nutz.walnut.ext.net.sendmail.hdl;

import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.net.sendmail.SendmailContext;
import org.nutz.walnut.ext.net.sendmail.SendmailFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class sendmail_vars extends SendmailFilter {

    @Override
    protected void process(WnSystem sys, SendmailContext fc, ZParams params) {
        // 读取标准输入
        if (params.vals.length == 0) {
            String json = sys.in.readAll();
            NutMap map = Lang.map(json);
            fc.vars.putAll(map);
        }
        // 指定了
        else {
            for (String str : params.vals) {
                NutMap map = Lang.map(str);
                fc.vars.putAll(map);
            }
        }
    }

}
