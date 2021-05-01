package org.nutz.walnut.ext.net.http.hdl;

import org.nutz.walnut.ext.net.http.HttpClientContext;
import org.nutz.walnut.ext.net.http.HttpClientFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class httpc_method extends HttpClientFilter {

    @Override
    protected void process(WnSystem sys, HttpClientContext fc, ZParams params) {
        String method = params.val(0);
        if (null != method) {
            fc.context.setMethod(method);
        }
    }

}
