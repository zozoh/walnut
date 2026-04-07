package com.site0.walnut.ext.net.mailx.hdl;

import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Ws;

import org.nutz.lang.util.NutMap;
import com.site0.walnut.ext.net.mailx.MailxContext;
import com.site0.walnut.ext.net.mailx.MailxFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

public class mailx_vars extends MailxFilter {

    @Override
    protected void process(WnSystem sys, MailxContext fc, ZParams params) {
        // 读取标准输入
        if (params.vals.length == 0) {
            String str = sys.in.readAll();
            String json = Ws.trim(str);
            NutMap map = Wlang.map(json);
            fc.vars.putAll(map);
        }
        // 指定了
        else {
            for (String str : params.vals) {
                String json = Ws.trim(str);
                NutMap map = Wlang.map(json);
                fc.vars.putAll(map);
            }
        }
    }

}
