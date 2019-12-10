package org.nutz.walnut.ext.www.impl;

import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.auth.WnAuthService;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.thing.util.Things;
import org.nutz.walnut.ext.www.bean.WnWebSite;
import org.nutz.walnut.impl.auth.AbstractWnAuthSetup;
import org.nutz.walnut.util.Wn;

public class WnWebAuthSetup extends AbstractWnAuthSetup {

    private WnWebSite site;

    public WnWebAuthSetup(WnIo io, WnWebSite site) {
        super(io);
        this.site = site;
    }

    @Override
    public String getDefaultRoleName() {
        if (site.hasRoleDir()) {
            WnQuery q = Wn.Q.pid(site.getRoleDir());
            q.setv("isdft", true);
            WnObj oR = io.getOne(q);
            if (null != oR) {
                return oR.name();
            }
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
        return site.getWeixinConf();
    }

    @Override
    protected WnObj createOrFetchAccountDir() {
        return site.getAccountDir();
    }

    @Override
    public WnObj getAvatarObj(WnAccount user, boolean autoCreate) {
        WnObj oData = Things.dirTsData(io, site.getAccountHome());
        String ph = Wn.appendPath(user.getId(), "thumb.jpg");
        if (autoCreate) {
            return io.createIfNoExists(oData, ph, WnRace.FILE);
        }
        return io.fetch(oData, ph);
    }

    @Override
    protected WnObj createOrFetchSessionDir() {
        return site.getSessionDir();
    }

    @Override
    protected WnObj createOrFetchCaptchaDir() {
        return site.getCaptchaDir();
    }

    @Override
    public void afterAccountCreated(WnAuthService auth, WnAccount user) {
        // 设置 Thing Set 的标准属性
        WnObj oU = io.checkById(user.getId());
        NutMap meta = Lang.map("th_live", Things.TH_LIVE);
        meta.put("th_set", site.getAccountHome().id());
        io.appendMeta(oU, meta);
    }

    @Override
    public boolean beforeAccountRenamed(WnAuthService auth, WnAccount user, String newName) {
        return true;
    }

    @Override
    public boolean beforeAccountDeleted(WnAuthService auth, WnAccount user) {
        return true;
    }

}
