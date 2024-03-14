package com.site0.walnut.ext.net.http.hdl;

import org.nutz.lang.util.NutMap;
import com.site0.walnut.ext.net.http.HttpClientContext;
import com.site0.walnut.ext.net.http.HttpClientFilter;
import com.site0.walnut.ext.net.http.cmd_httpc;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

public class httpc_params extends HttpClientFilter {

    @Override
    protected void process(WnSystem sys, HttpClientContext fc, ZParams params) {
        NutMap q = cmd_httpc.evalQuery(sys, params, true);
        fc.context.addParams(q);
    }

}
