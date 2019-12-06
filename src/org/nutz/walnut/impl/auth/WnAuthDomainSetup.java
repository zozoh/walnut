package org.nutz.walnut.impl.auth;

import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.auth.WnAuthSite;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.util.Wn;

public class WnAuthDomainSetup extends AbstractWnAuthSetup {

    private WnAuthSite site;

    public WnAuthDomainSetup(WnIo io, WnAuthSite site) {
        super(io);
        this.site = site;
    }

    @Override
    public String getDefaultRoleName() {
        return null;
    }

    @Override
    public long getSessionDefaultDuration() {
        return site.getSessionDuration();
    }

    @Override
    protected WnObj getWeixinConf() {
        if (null != site.getWeixinConfPath()) {
            return Wn.checkObj(io, site.getWeixinConfPath());
        }
        return null;
    }

    @Override
    protected WnObj createOrFetchAccountDir() {
        return Wn.checkObj(io, site.getAccounts());
    }

    @Override
    protected WnObj createOrFetchSessionDir() {
        return Wn.checkObj(io, site.getSessions());
    }

    @Override
    protected WnObj createOrFetchCaptchaDir() {
        return Wn.checkObj(io, site.getCaptchas());
    }

    @Override
    public void afterAccountCreated(WnAccount user) {}

    @Override
    public void afterAccountRenamed(WnAccount user) {}

    @Override
    public boolean beforeAccountDeleted(WnAccount user) {
        return false;
    }
}
