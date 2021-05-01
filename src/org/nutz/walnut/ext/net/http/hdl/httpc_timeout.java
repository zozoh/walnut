package org.nutz.walnut.ext.net.http.hdl;

import org.nutz.walnut.ext.net.http.HttpClientContext;
import org.nutz.walnut.ext.net.http.HttpClientFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class httpc_timeout extends HttpClientFilter {

    @Override
    protected void process(WnSystem sys, HttpClientContext fc, ZParams params) {
        int readTimeout = params.val_int(0, -1);
        if (readTimeout > 0) {
            fc.context.setReadTimeout(readTimeout);
        }
        int connTimeout = params.val_int(1, -1);
        if (connTimeout > 0) {
            fc.context.setConnectTimeout(connTimeout);
        }
    }

}
