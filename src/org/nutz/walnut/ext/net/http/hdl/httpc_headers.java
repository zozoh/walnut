package org.nutz.walnut.ext.net.http.hdl;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.net.http.HttpClientContext;
import org.nutz.walnut.ext.net.http.HttpClientFilter;
import org.nutz.walnut.ext.net.util.WnNet;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

public class httpc_headers extends HttpClientFilter {

    @Override
    protected void process(WnSystem sys, HttpClientContext fc, ZParams params) {
        // 依次读取
        NutMap h = new NutMap();
        if (null != params.vals) {
            for (String val : params.vals) {
                // 采用标准浏览器头
                NutBean std = WnNet.getBrowserHeader(val);
                if (null != std) {
                    h.putAll(std);
                }
                // 自定义头
                else {
                    WnNet.parseQueryTo(h, val, false);
                }
            }
        }
        // 从文件读取里
        else if (params.has("f")) {
            String ph = params.getString("f");
            WnObj o = Wn.checkObj(sys, ph);
            String input = sys.io.readText(o);
            WnNet.parseQueryTo(h, input, false);
        }
        // 从标准输入读取
        else {
            String input = sys.in.readAll();
            WnNet.parseQueryTo(h, input, false);
        }
        fc.context.addHeaders(h);
    }

}
