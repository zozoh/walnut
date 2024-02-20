package org.nutz.walnut.ext.net.http.hdl;

import java.io.InputStream;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.net.http.HttpClientContext;
import org.nutz.walnut.ext.net.http.HttpClientFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

public class httpc_body extends HttpClientFilter {

    @Override
    protected void process(WnSystem sys, HttpClientContext fc, ZParams params) {
        // 依次读取
        if (params.vals.length > 0) {
            String body = params.val(0);
            fc.context.setBody(body);
        }
        // 从文件读取里
        else if (params.has("f")) {
            String ph = params.getString("f");
            WnObj o = Wn.checkObj(sys, ph);
            InputStream ins = sys.io.getInputStream(o, 0);
            fc.context.setBody(ins);
        }
        // 从标准输入读取
        else {
            String input = sys.in.readAll();
            fc.context.setBody(input);
        }
    }

}
