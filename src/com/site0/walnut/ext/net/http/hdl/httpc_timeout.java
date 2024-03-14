package com.site0.walnut.ext.net.http.hdl;

import com.site0.walnut.ext.net.http.HttpClientContext;
import com.site0.walnut.ext.net.http.HttpClientFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

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
