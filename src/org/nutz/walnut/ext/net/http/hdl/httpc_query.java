package org.nutz.walnut.ext.net.http.hdl;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.net.http.HttpClientContext;
import org.nutz.walnut.ext.net.http.HttpClientFilter;
import org.nutz.walnut.ext.net.http.cmd_httpc;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class httpc_query extends HttpClientFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(reset|decode)$");
    }

    @Override
    protected void process(WnSystem sys, HttpClientContext fc, ZParams params) {
        if (params.is("reset")) {
            fc.context.clearQuery();
        }
        NutMap q = cmd_httpc.evalQuery(sys, params, false);
        fc.context.addQuery(q);
    }

}
