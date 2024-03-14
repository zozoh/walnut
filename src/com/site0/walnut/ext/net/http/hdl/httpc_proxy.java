package com.site0.walnut.ext.net.http.hdl;

import java.net.InetSocketAddress;
import java.net.Proxy;

import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.net.http.HttpClientContext;
import com.site0.walnut.ext.net.http.HttpClientFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

public class httpc_proxy extends HttpClientFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(socket|auto)");
    }

    @Override
    protected void process(WnSystem sys, HttpClientContext fc, ZParams params) {
        int port = params.val_int(0, 0);
        String host = params.val(1, "127.0.0.1");
        boolean asSocket = params.is("socket");
        boolean isAuto = params.is("auto");
        
        // 配置文件路径
        String ph = params.getString("f");
        if (isAuto && Ws.isBlank(ph)) {
            ph = "~/.domain/proxy.json";
        }
        
        // 检查配置文件（优先），参数的作为默认值
        if (!Ws.isBlank(ph)) {
            WnObj o = Wn.getObj(sys, ph);
            if (null != o) {
                port = params.val_int(0, 80);
                host = params.val(1, "127.0.0.1");
                NutMap proxy = sys.io.readJson(o, NutMap.class);
                port = proxy.getInt("port", port);
                host = proxy.getString("host", host);
                asSocket = proxy.getString("type", "http").equals("socket");
            }
        }
        // 没有配置，则一定用参数
        else {
            // 貌似不用做什么
        }

        if (port > 0 && !Ws.isBlank(host)) {
            Proxy proxy;
            if (asSocket) {
                proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(host, port));
                fc.context.setProxy(proxy);
            } else {
                proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
                fc.context.setProxy(proxy);
            }
        }
    }

}
