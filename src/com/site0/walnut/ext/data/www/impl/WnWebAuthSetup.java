package com.site0.walnut.ext.data.www.impl;

import com.site0.walnut.util.Wlang;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.auth.WnAccount;
import com.site0.walnut.api.auth.WnAuthService;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.ext.data.thing.util.Things;
import com.site0.walnut.ext.data.www.bean.WnWebSite;
import com.site0.walnut.impl.auth.AbstractWnAuthSetup;
import com.site0.walnut.util.Wn;

public class WnWebAuthSetup extends AbstractWnAuthSetup {

    private WnWebSite site;

    public WnWebAuthSetup(WnIo io, WnWebSite site) {
        super(io);
        this.site = site;
    }

    @Override
    public WnObj getRoleDir() {
        return this.site.getRoleDir();
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
    public int getSessionDefaultDuration() {
        return site.getSeDftDu();
    }

    @Override
    public int getSessionTransientDuration() {
        return site.getSeTmpDu();
    }

    @Override
    protected WnObj getWeixinConf(String codeType) {
        return site.getWeixinConf(codeType);
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
        NutMap meta = Wlang.map("th_live", Things.TH_LIVE);
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
