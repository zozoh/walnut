package com.site0.walnut.ext.net.webx;

import org.nutz.log.Log;

import com.site0.walnut.impl.box.JvmFilterExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.util.ZParams;

public class cmd_webx extends JvmFilterExecutor<WebxContext, WebxFilter> {

    private static final Log log = Wlog.getCMD();

    public cmd_webx() {
        super(WebxContext.class, WebxFilter.class);
    }

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "cqnl");
    }

    @Override
    protected WebxContext newContext() {
        return new WebxContext();
    }

    @Override
    protected void prepare(WnSystem sys, WebxContext fc) {
        
    }

    @Override
    protected void output(WnSystem sys, WebxContext fc) {

    }
}
