package org.nutz.walnut.ext.net.http.hdl;

import java.net.InetSocketAddress;
import java.net.Proxy;

import org.nutz.walnut.ext.net.http.HttpClientContext;
import org.nutz.walnut.ext.net.http.HttpClientFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class httpc_proxy extends HttpClientFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(socket)");
    }

    @Override
    protected void process(WnSystem sys, HttpClientContext fc, ZParams params) {
        int port = params.val_check_int(0);
        String host = params.val(1, "127.0.0.1");

        Proxy proxy;
        if (params.is("socket")) {
            proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(host, port));
            fc.context.setProxy(proxy);
        } else {
            proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
            fc.context.setProxy(proxy);
        }
    }

}
