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

    /**
     * 站点所在域的主目录路径。这个参数之所以必须是因为它在设置时会自动给 vars 添加 <code>HOME</code> 键，这样，各种指定的
     * session/accounts ... 之类的路径就能支持 <code>~/</code> 前缀了。
     */
    private String domainHomePath;
    private NutMap vars;

    private String siteId;

    private WnObj sessionDir;
    private WnObj captchaDir;
    /**
     * 账户库所在目录(Thing)
     */
    private WnObj accountHome;
    /**
     * 账户库的索引目录
     */
    private WnObj accountDir;
    /**
     * 角色库的索引目录
     */
    private WnObj roleDir;
    /**
     * 订单库所在目录(Thing)
     */
    private WnObj orderHome;
    /**
     * 优惠券库所在目录(Thing)
     */
    private WnObj couponHome;

    /**
     * 地址库所在目录(Thing)
     */
    private WnObj addressesHome;

    /**
     * 运费表对象
     */
    private WnObj freightSheetObj;

    /**
     * 微信配置目录名
     */
    private NutMap weixin;
    /**
     * 支付商户名集合
     */
    private NutMap sellers;

    /**
     * 支付采用的默认货币结算单位 默认 RMB
     */
    private String currency;

    /**
     * 站点的订单默认过期时间（分钟）
     * <ul>
     * <li>0 或者负数 表示永不过期
     * <li>>0 表示秒
     * <li>默认为 15 分钟
     * </ul>
     */
    private int orderDuMin;

    /**
     * 默认会话时长（秒）
     */
    private int seDftDu;

    /**
     * 临时会话时长（秒）
     */
    private int seTmpDu;

    /**
     * 站点的接口，请求对象缓存默认时长
     * <ul>
     * <li>0 表示请求完毕立即删除
     * <li>> 0 表示 秒
     * <li>负数相当于没有设置
     * </ul>
     */
    private int apiReqDu;

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
        sessionDir = checkDirOrCreate("~/.domain/session/" + siteId);
        captchaDir = checkDirOrCreate("~/.domain/captcha/" + siteId);

        // 账户/角色库路径
        accountDir = checkThingIndex(bean.getString("accounts"));
        accountHome = accountDir.parent();
        roleDir = getThingIndex(bean.getString("roles"));

        // 支付相关： 产品/订单/优惠券的库（不是索引index，而是库的主目录，必须为 ThingSet）
        orderHome = fetchThingSet(bean.getString("orders"));
        couponHome = fetchThingSet(bean.getString("coupons"));

        // 地址库
        addressesHome = fetchThingSet(bean.getString("addresses"));
        freightSheetObj = Wn.getObj(io, vars, bean.getString("freight_sheet"));

        // 初始化站点用户默认系统环境变量
        if (bean.has("env")) {
            NutMap env = bean.getAs("env", NutMap.class);
            this.vars.putAll(env);
        }

        // 获取微信配置目录名
        if (bean.has("weixin")) {
            weixin = bean.getAs("weixin", NutMap.class);
        }

        // 获取支付商户号配置
        if (bean.has("sellers")) {
            sellers = bean.getAs("sellers", NutMap.class);
        }

        // 获取默认货币单位
        this.currency = bean.getString("currency", "RMB");

        // 默认会话时长
        seDftDu = bean.getInt("se_dft_du", 86400);
        seTmpDu = bean.getInt("se_tmp_du", 60);

        // 站点的订单默认过期时间
        orderDuMin = bean.getInt("order_du_min", 15);

        // 请求对象缓存默认时长
        apiReqDu = bean.getInt("api_req_du", -1);

    }

    public String getDomainHomePath() {
        return domainHomePath;
    }

    public void setDomainHomePath(String domainHomePath) {
        this.domainHomePath = domainHomePath;
        this.vars = Lang.map("HOME", domainHomePath);
    }

    public String getDomainGroup() {
        return accountDir.group();
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public NutMap getVars() {
        return vars;
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

    public boolean hasCouponHome() {
        return null != couponHome;
    }

    public WnObj getCouponHome() {
        return couponHome;
    }

    public boolean hasAddressesHome() {
        return null != addressesHome;
    }

    public WnObj getAddressesHome() {
        return addressesHome;
    }

    public boolean hasFreightSheetObj() {
        return null != freightSheetObj;
    }

    public WnObj getFreightSheetObj() {
        return freightSheetObj;
    }

    /**
     * @param codeType
     *            公号类型(mp | gh | open)
     * @return 是否存在指定类型的微信配置
     */
    public boolean hasWeixinConf(String codeType) {
        return null != weixin && weixin.has(codeType);
    }

    /**
     * @param codeType
     *            公号类型(mp | gh | open)
     * @return 微信配置文件对象
     */
    public WnObj getWeixinConf(String codeType) {
        if (!hasWeixinConf(codeType)) {
            return null;
        }
        String confName = weixin.getString(codeType);
        String ph = "~/.weixin/" + confName + "/wxconf";
        return this.checkDirOrFile(ph);
    }

    public boolean hasSellers() {
        return null != sellers && !sellers.isEmpty();
    }

    public NutMap getSellers() {
        return sellers;
    }

    public String getSellerName(String nameOrPayType) {
        if (null == sellers || Strings.isBlank(nameOrPayType)) {
            return null;
        }
        // 如果是支付类型，譬如:
        // - wx.qrcode : 微信主动扫二维码付款
        // - wx.jsapi : 微信公众号内支付
        // - wx.scan : 微信被物理码枪扫付款码支付
        // - zfb.qrcode : 支付宝主动扫二维码付款
        // - zfb.scan : 支付宝被物理码枪扫付款码支付
        // 需要之取前缀
        int pos = nameOrPayType.indexOf('.');
        String sellerName = pos > 0 ? nameOrPayType.substring(0, pos) : nameOrPayType;
        return sellers.getString(sellerName);
    }

    public String getCurrency() {
        return currency;
    }

    public int getSeDftDu() {
        return seDftDu;
    }

    public int getSeTmpDu() {
        return seTmpDu;
    }

    public int getOrderDuMin() {
        return orderDuMin;
    }

    public int getApiReqDu() {
        return apiReqDu;
    }

    private WnObj checkDirOrCreate(String ph) {
        String aph = Wn.normalizeFullPath(ph, vars);
        return io.createIfNoExists(null, aph, WnRace.DIR);
    }

    private WnObj checkThingIndex(String ph) {
        String aph = Wn.normalizeFullPath(ph, vars);
        WnObj oDir = io.check(null, aph);
        // 必须是 ThingSet 的索引目录
        return Things.dirTsIndex(io, oDir);
    }

    private WnObj getThingIndex(String ph) {
        if (Strings.isBlank(ph)) {
            return null;
        }
        String aph = Wn.normalizeFullPath(ph, vars);
        WnObj oDir = io.fetch(null, aph);
        if (null == oDir)
            return null;
        // 必须是 ThingSet 的索引目录
        return Things.dirTsIndex(io, oDir);
    }

    private WnObj fetchThingSet(String ph) {
        String aph = Wn.normalizeFullPath(ph, vars);
        if (!Strings.isBlank(aph)) {
            WnObj oDir = io.fetch(null, aph);
            // 尝试获取 ThingSet
            if (null != oDir)
                return Things.getThingSet(oDir);
        }
        return null;
    }

    private WnObj checkDirOrFile(String ph) {
        String aph = Wn.normalizeFullPath(ph, vars);
        return io.check(null, aph);
    }
}
