package org.nutz.walnut.ext.data.site;

import org.nutz.walnut.impl.box.JvmFilterExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

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
        String sitePath = fc.params.val_check(0);
        fc.oSite = Wn.checkObj(sys, sitePath);
    }

    @Override
    protected void output(WnSystem sys, SiteContext fc) {}

}
