package org.nutz.walnut.ext.net.xapi.hdl;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.net.xapi.XApi;
import org.nutz.walnut.ext.net.xapi.cmd_xapi;
import org.nutz.walnut.ext.net.xapi.bean.XApiRequest;
import org.nutz.walnut.ext.net.xapi.impl.WnXApi;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.Ws;

@JvmHdlParamArgs(value = "cnq", regex = "^(force)$")
public class xapi_send implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 首先读取变量，变量与会话的变量合并，给上下文更多的信息
        NutMap vars = cmd_xapi.loadVars(sys, hc);

        // 准备请求路径
        String apiName = hc.params.val_check(0);
        String account = hc.params.val_check(1);
        String path = hc.params.val_check(2);
        String proxyPath = hc.params.getString("proxy", "~/.domain/proxy.json");
        boolean force = hc.params.is("force");

        // 准备 API
        XApi api = new WnXApi(sys);

        // 准备代理
        setupProxy(sys, proxyPath, api);

        // 获取请求对象
        XApiRequest req = api.prepare(apiName, account, path, vars, false);
        req.setDisableCache(force);

        // 发送请求，并且将流输出
        InputStream ins = api.send(req, InputStream.class);
        sys.out.writeAndClose(ins);

    }

    public static void setupProxy(WnSystem sys, String proxyPath, XApi api) {
        // InetSocketAddress addr = new InetSocketAddress("127.0.0.1", 10080);
        // Proxy proxy = new Proxy(Proxy.Type.HTTP, addr);
        // api.setProxy(proxy);
        if (!Ws.isBlank(proxyPath) && !"none".equals(proxyPath)) {
            WnObj oProxy = Wn.getObj(sys, proxyPath);
            if (null != oProxy) {
                NutMap proxyConf = sys.io.readJson(oProxy, NutMap.class);
                String host = proxyConf.getString("host");
                int port = proxyConf.getInt("port");
                String type = proxyConf.getString("type", "http");
                if (!Ws.isBlank(host) && port > 0) {
                    Proxy.Type pxType = Proxy.Type.valueOf(type.toUpperCase());
                    InetSocketAddress addr = new InetSocketAddress(host, port);
                    Proxy proxy = new Proxy(pxType, addr);
                    api.setProxy(proxy);

                }
            }
        }
    }

}
