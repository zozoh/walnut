package org.nutz.walnut.ext.net.http.hdl;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.net.http.HttpClientContext;
import org.nutz.walnut.ext.net.http.HttpClientFilter;
import org.nutz.walnut.ext.net.http.cmd_httpc;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class httpc_params extends HttpClientFilter {

    @Override
    protected void process(WnSystem sys, HttpClientContext fc, ZParams params) {
        NutMap q = cmd_httpc.evalQuery(sys, params, true);
        fc.context.addParams(q);
    }

}
