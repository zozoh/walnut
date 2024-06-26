package com.site0.walnut.ext.net.http.hdl;

import java.io.InputStream;

import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.net.http.HttpClientContext;
import com.site0.walnut.ext.net.http.HttpClientFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.ZParams;

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
