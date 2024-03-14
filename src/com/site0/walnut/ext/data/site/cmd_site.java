package com.site0.walnut.ext.data.site;

import com.site0.walnut.impl.box.JvmFilterExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

public class cmd_site extends JvmFilterExecutor<SiteContext, SiteFilter> {

    public cmd_site() {
        super(SiteContext.class, SiteFilter.class);
    }

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "cqnl", "^(noexists)$");
    }

    @Override
    protected SiteContext newContext() {
        return new SiteContext();
    }

    @Override
    protected void prepare(WnSystem sys, SiteContext fc) {
        String sitePath = fc.params.val(0);
        if (!Ws.isBlank(sitePath)) {
            fc.oSite = Wn.checkObj(sys, sitePath);
        }
    }

    @Override
    protected void output(WnSystem sys, SiteContext fc) {}

}
