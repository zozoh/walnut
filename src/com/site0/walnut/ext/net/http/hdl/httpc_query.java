package com.site0.walnut.ext.net.http.hdl;

import org.nutz.lang.util.NutMap;
import com.site0.walnut.ext.net.http.HttpClientContext;
import com.site0.walnut.ext.net.http.HttpClientFilter;
import com.site0.walnut.ext.net.http.cmd_httpc;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

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
