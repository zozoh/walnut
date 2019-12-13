package org.nutz.walnut.ext.www.bean;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.thing.util.Things;
import org.nutz.walnut.util.Wn;

public class WnWebSite {

    private WnIo io;

    private String domainHomePath;
    private NutMap vars;

    private String siteId;

    private WnObj sessionDir;
    private WnObj captchaDir;
    private WnObj accountHome;
    private WnObj accountDir;
    private WnObj roleDir;
    private WnObj orderHome;
    private WnObj productHome;
    private WnObj couponHome;
    private WnObj weixinConf;

    private NutMap sellers;

    private long seDftDu;

    private long seTmpDu;

    /**
     * @param homeFullPath
     *            所在域的主目录路径
     * 
     * @param siteId
     *            站点 ID
     * @param bean
     *            配置集合。请参看相关文档，关于一个站点的元数据描述
     */
    public WnWebSite(WnIo io, String domainHomePath, String siteId, NutBean bean) {
        if (Strings.isBlank(domainHomePath)) {
            throw Er.create("e.auth.site.NoDomainHomePath");
        }
        if (Strings.isBlank(siteId)) {
            throw Er.create("e.auth.site.siteId");
        }
        this.io = io;
        this.siteId = siteId;
        this.setDomainHomePath(domainHomePath);
        this.valueOf(bean);
    }

    /**
     * 从一个配置集合里设置各个字段信息
     * 
     * @param bean
     *            配置集合。请参看相关文档，关于一个站点的元数据描述
     */
    public void valueOf(NutBean bean) {
        // 会话和验证码
        sessionDir = CheckDirOrCreate("~/.domain/session/" + siteId);
        captchaDir = CheckDirOrCreate("~/.domain/captcha/" + siteId);

        // 账户/角色库路径
        accountDir = CheckThingIndex(bean.getString("accounts"));
        accountHome = accountDir.parent();
        roleDir = CheckThingIndex(bean.getString("roles"));

        // 支付相关： 产品/订单/优惠券的库（不是索引index，而是库的主目录，必须为 ThingSet）
        orderHome = FetchThingSet(bean.getString("orders"));
        productHome = FetchThingSet(bean.getString("products"));
        couponHome = FetchThingSet(bean.getString("coupons"));

        // 获取支付商户号配置
        // 同时获取微信配置文件路径，以便 weixin 设置如果为空，尝试用 sellers.wx
        String wxConfNm = null;
        if (bean.has("sellers")) {
            sellers = bean.getAs("sellers", NutMap.class);
            wxConfNm = sellers.getString("wx");
        }

        // 微信配置文件路径
        wxConfNm = bean.getString("weixin", wxConfNm);
        if (!Strings.isBlank(wxConfNm)) {
            weixinConf = CheckDirOrFile("~/.weixin/" + wxConfNm + "/wxconf");
        }

        // 默认会话时长
        seDftDu = bean.getLong("se_dft_du", 86400);
        seTmpDu = bean.getLong("se_tmp_du", 60);
    }

    public String getDomainHomePath() {
        return domainHomePath;
    }

    public void setDomainHomePath(String domainHomePath) {
        this.domainHomePath = domainHomePath;
        this.vars = Lang.map("HOME", domainHomePath);
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public WnObj getSessionDir() {
        return sessionDir;
    }

    public WnObj getCaptchaDir() {
        return captchaDir;
    }

    public WnObj getAccountHome() {
        return accountHome;
    }

    public WnObj getAccountDir() {
        return accountDir;
    }

    public boolean hasRoleDir() {
        return null != roleDir;
    }

    public WnObj getRoleDir() {
        return roleDir;
    }

    public boolean hasOrderHome() {
        return null != orderHome;
    }

    public WnObj getOrderHome() {
        return orderHome;
    }

    public boolean hasProductHome() {
        return null != productHome;
    }

    public WnObj getProductHome() {
        return productHome;
    }

    public boolean hasCouponHome() {
        return null != couponHome;
    }

    public WnObj getCouponHome() {
        return couponHome;
    }

    public boolean hasWeixinConf() {
        return null != weixinConf;
    }

    public WnObj getWeixinConf() {
        return weixinConf;
    }

    public boolean hasSellers() {
        return null != sellers && !sellers.isEmpty();
    }

    public NutMap getSellers() {
        return sellers;
    }

    public long getSeDftDu() {
        return seDftDu;
    }

    public long getSeTmpDu() {
        return seTmpDu;
    }

    private WnObj CheckDirOrCreate(String ph) {
        String aph = Wn.normalizeFullPath(ph, vars);
        return io.createIfNoExists(null, aph, WnRace.DIR);
    }

    private WnObj CheckThingIndex(String ph) {
        String aph = Wn.normalizeFullPath(ph, vars);
        WnObj oDir = io.check(null, aph);
        // 必须是 ThingSet 的索引目录
        return Things.dirTsIndex(io, oDir);
    }

    private WnObj FetchThingSet(String ph) {
        String aph = Wn.normalizeFullPath(ph, vars);
        if (!Strings.isBlank(aph)) {
            WnObj oDir = io.fetch(null, aph);
            // 尝试获取 ThingSet
            if (null != oDir)
                return Things.getThingSet(oDir);
        }
        return null;
    }

    private WnObj CheckDirOrFile(String ph) {
        String aph = Wn.normalizeFullPath(ph, vars);
        return io.check(null, aph);
    }
}
