package com.site0.walnut.ext.net.http.hdl;

import com.site0.walnut.ext.net.http.HttpClientContext;
import com.site0.walnut.ext.net.http.HttpClientFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

public class httpc_method extends HttpClientFilter {

    @Override
    protected void process(WnSystem sys, HttpClientContext fc, ZParams params) {
        String method = params.val(0);
        if (null != method) {
            fc.context.setMethod(method);
        }
    }

}
