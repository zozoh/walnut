package org.nutz.walnut.impl.auth;

import org.nutz.lang.Strings;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.auth.WnAuthSite;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.util.Wn;

public class WnDomainAuthSetup extends AbstractWnAuthSetup {

    private WnAuthSite site;

    private WnObj oRoleDir;

    public WnDomainAuthSetup(WnIo io, WnAuthSite site) {
        super(io);
        this.site = site;
        if (!Strings.isBlank(site.getRoles())) {
            String ph = site.getRoles();
            if (!ph.endsWith("/index")) {
                ph = Wn.appendPath(ph, "index");
            }
            oRoleDir = Wn.checkObj(io, ph);
        }
    }

    @Override
    public String getDefaultRoleName() {
        WnQuery q = Wn.Q.pid(oRoleDir);
        q.setv("isdft", true);
        WnObj oR = io.getOne(q);
        if (null != oR) {
            return oR.name();
        }
        return "user";
    }

    @Override
    public long getSessionDefaultDuration() {
        return site.getSeDftDu();
    }

    @Override
    public long getSessionTransientDuration() {
        return site.getSeTmpDu();
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
        String ph = site.getAccounts();
        if (!ph.endsWith("/index")) {
            ph = Wn.appendPath(ph, "index");
        }
        return Wn.checkObj(io, ph);
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
        return true;
    }
}
