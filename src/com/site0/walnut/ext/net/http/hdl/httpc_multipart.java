package com.site0.walnut.ext.net.http.hdl;

import java.util.List;

import org.nutz.json.Json;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.net.http.HttpClientContext;
import com.site0.walnut.ext.net.http.HttpClientFilter;
import com.site0.walnut.ext.net.http.bean.HttpFormPart;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

public class httpc_multipart extends HttpClientFilter {

    @Override
    protected void process(WnSystem sys, HttpClientContext fc, ZParams params) {
        // 依次读取
        if (null != params.vals && params.vals.length > 0) {
            for (String val : params.vals) {
                joinFormParts(fc, val);
            }
        }
        // 从文件读取里
        else if (params.has("f")) {
            String ph = params.getString("f");
            WnObj o = Wn.checkObj(sys, ph);
            String input = sys.io.readText(o);
            joinFormParts(fc, input);
        }
        // 从标准输入读取
        else {
            String input = sys.in.readAll();
            joinFormParts(fc, input);
        }

    }

    private void joinFormParts(HttpClientContext fc, String val) {
        // 指定的是一个数组
        if (Ws.isQuoteBy(val, '[', ']')) {
            List<HttpFormPart> ps = Json.fromJsonAsList(HttpFormPart.class, val);
            fc.context.addFormParts(ps);
        }
        // 独立的对象吗？
        else {
            HttpFormPart part = Json.fromJson(HttpFormPart.class, val);
            fc.context.addFormPart(part);
        }
    }

}
