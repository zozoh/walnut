package com.site0.walnut.ext.data.www.bean;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nutz.dao.Dao;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.auth.WnOrganization;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.ext.data.entity.history.HistoryApi;
import com.site0.walnut.ext.data.entity.history.HistoryConfig;
import com.site0.walnut.ext.data.entity.history.HistoryRecord;
import com.site0.walnut.ext.data.entity.history.WnHistoryService;
import com.site0.walnut.ext.data.thing.util.Things;
import com.site0.walnut.ext.sys.sql.WnDaos;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;

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
     * 账户库所在目录(Thing)
     */
    private WnObj roleHome;
    /**
     * 角色库的索引目录
     */
    private WnObj roleDir;
    /**
     * 组织结构树所在文件
     */
    private WnObj organizationObj;
    /**
     * 组织结构树
     */
    private WnOrganization organization;

    /**
     * 订单库所在目录(Thing)
     */
    private WnObj orderHome;
    /**
     * 优惠券库所在目录(Thing)
     */
    private WnObj couponHome;

    /**
     * 采用什么命令获取公司/组织/机构的列表
     */
    private String companyBy;

    /**
     * 采用什么命令获取(公司/组织/机构)的组织结构图
     * <p>
     * 这个命令返回通常是一个 <code>{id,name,children}</code>格式的树形式数据<br>
     * 并且这个命令也是一个模板，因为需要从<code>companyBy</code>获取机构信息。
     * 占位符通常为<code>{id}</code>，当然，根据你的机构元数据，你可以随意使用占位符
     */
    private String deptBy;

    /**
     * 自定义权限中， owner 的默认权限，默认 7
     */
    private int pvgOwner;

    /**
     * 自定义权限中， memeber 的默认权限，默认 5
     */
    private int pvgMember;

    /**
     * 采用什么命令获取工作项目（业务上）的列表
     */
    private String projectBy;

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
     * 【选】站点如果采用了动态价格规则，商品对应的商品规则对象中（即【商品包】） 哪个键用来声明商品的的建议零售价。
     * 因为，通常这种场景，商品包里会包含多个商品，建议零售价是完全一致的。 如果站点不声明这个选项，商品的建议零售价会采用自身的元数据 `price`
     * 来决定
     */
    private String priceRetailKey;

    /**
     * 基础价格类型
     */
    private OrderFeeMode feeMode;

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
     * 哪些行为要记录历史
     */
    private NutMap history;

    /**
     * 历史记录的源，默认为 _history
     */
    private String hisname;

    /**
     * 缓存历史记录对象的实例
     */
    private HistoryApi historyApi;

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
        String accountPath = bean.getString("accounts");
        if (Ws.isBlank(accountPath)) {
            throw Er.create("e.www.WebSiteWithoutAccounts", this.siteId);
        }
        accountDir = checkThingIndex(accountPath);
        accountHome = accountDir.parent();
        roleDir = getThingIndex(bean.getString("roles"));
        if (null != roleDir) {
            roleHome = roleDir.parent();
        }

        // 组织结构
        String orgPh = bean.getString("organization");
        if (!Ws.isBlank(orgPh)) {
            organizationObj = this.checkDirOrFile(orgPh);
        }

        // 公司/组织/结构，组织结构图，业务项目等
        companyBy = bean.getString("companyBy");
        deptBy = bean.getString("deptBy");
        pvgOwner = bean.getInt("pvg_owner", 7);
        pvgMember = bean.getInt("pvg_member", 5);
        projectBy = bean.getString("projectBy");

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

        // 确定动态价格规划场景下的商品建立零售价，在商品包中的键
        this.priceRetailKey = bean.getString("price_retail_key");

        // 确认基础价格类型
        this.feeMode = bean.getEnum("fee_mode", OrderFeeMode.class);
        if (null == this.feeMode) {
            this.feeMode = OrderFeeMode.TOTAL;
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

        // 历史记录
        history = bean.getAs("history", NutMap.class);
        hisname = bean.getString("hisname", "_history");
    }

    public boolean hasHistory() {
        return null != history && history.size() > 0;
    }

    public Set<String> getHistoryEventNames() {
        if (null == history)
            return new HashSet<>();
        return history.keySet();
    }

    @SuppressWarnings("unchecked")
    public List<NutBean> getHistoryTmpls(String key) {
        List<NutBean> list = new LinkedList<>();
        if (null == history) {
            return list;
        }
        Object obj = history.get(key);
        if (null == obj)
            return list;

        // 多个模板
        if (obj instanceof Collection<?>) {
            Collection<?> coll = (Collection<?>) obj;
            for (Object ele : coll) {
                if (ele instanceof Map) {
                    NutMap eMap = NutMap.WRAP((Map<String, Object>) ele);
                    list.add(eMap);
                }
            }
        }
        // 单一模板
        else if (obj instanceof Map) {
            NutMap eMap = NutMap.WRAP((Map<String, Object>) obj);
            list.add(eMap);
        }

        return list;
    }

    public void addHistoryRecord(NutBean context, String key) {
        List<NutBean> hisTmpls = this.getHistoryTmpls(key);
        this.addHistoryRecord(context, hisTmpls);
    }

    public void addHistoryRecord(NutBean context, List<NutBean> hisTmpls) {
        if (null == context || null == hisTmpls || hisTmpls.isEmpty())
            return;

        // 获取历史记录 API
        HistoryApi api = this.getHistoryApi();
        if (null == api) {
            return;
        }

        // 插入多条历史记录
        for (NutBean hisTmpl : hisTmpls) {
            NutMap hisre = (NutMap) Wn.explainObj(context, hisTmpl);

            // 插入历史记录
            HistoryRecord his = Wlang.map2Object(hisre, HistoryRecord.class);
            api.add(his);
        }
    }

    public HistoryApi getHistoryApi() {
        if (hasHistory()) {
            if (null == historyApi) {
                String ph = "~/.domain/history/" + hisname + ".json";
                String aph = Wn.normalizeFullPath(ph, vars);
                WnObj oHis = io.check(null, aph);
                HistoryConfig conf = WnDaos.loadConfig(HistoryConfig.class, io, oHis, vars);
                Dao dao = WnDaos.get(conf.getAuth());
                historyApi = new WnHistoryService(conf, dao);
            }
        }
        return historyApi;
    }

    public String getDomainHomePath() {
        return domainHomePath;
    }

    public void setDomainHomePath(String domainHomePath) {
        this.domainHomePath = domainHomePath;
        this.vars = Wlang.map("HOME", domainHomePath);
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

    public boolean hasAccountHome() {
        return null != accountHome;
    }

    public WnObj getAccountHome() {
        return accountHome;
    }

    public boolean hasAccountDir() {
        return null != accountDir;
    }

    public WnObj getAccountDir() {
        return accountDir;
    }

    public boolean hasRoleHome() {
        return null != roleHome;
    }

    public WnObj getRoleHome() {
        return roleHome;
    }

    public boolean hasRoleDir() {
        return null != roleDir;
    }

    public boolean hasOrganization() {
        return null != this.organizationObj;
    }

    public WnObj getOrganizationObj() {
        return organizationObj;
    }

    public WnOrganization getOrganization() {
        if (this.hasOrganization()) {
            if (null == organization) {
                organization = io.readJson(organizationObj, WnOrganization.class);
            }
        }
        return organization;
    }

    public WnObj fetchRole(String roleName) {
        return io.fetch(roleDir, roleName);
    }

    public WnObj checkRole(String roleName) {
        return io.check(roleDir, roleName);
    }

    public String readRole(String roleName) {
        WnObj o = io.check(roleDir, roleName);
        return io.readText(o);
    }

    public <T> T readRoleAs(String roleName, Class<T> classOfT) {
        WnObj o = io.check(roleDir, roleName);
        return io.readJson(o, classOfT);
    }

    public NutBean readRoleAsJson(String roleName) {
        WnObj o = io.fetch(roleDir, roleName);
        if (null == o) {
            return null;
        }
        return io.readJson(o, NutMap.class);
    }

    public NutMap getRoleAllowActions(String roleName) {
        NutMap re = null;
        WnObj o = io.fetch(roleDir, roleName);
        if (null != o) {
            // return o.getAsList(WnAccount.K_ROLE_ACTIONS, String.class);
            re = io.readJson(o, NutMap.class);
        }
        if (null == re) {
            return new NutMap();
        }
        return re;
    }

    public Collection<String> getOrgAllowActions(String[] depts) {
        HashSet<String> as = new HashSet<>();
        if (null != depts && depts.length > 0 && this.hasOrganization()) {
            // 读取组织结构设置
            WnOrganization wo = this.getOrganization();

            // 准备一个自己所在部门的映射表
            HashMap<String, Boolean> map = new HashMap<>();
            for (String dept : depts) {
                map.put(dept, true);
            }

            // 记入结果
            wo.joinRoleActions(as, deptId -> map.containsKey(deptId));

        }
        return as;
    }

    public WnObj getRoleDir() {
        return roleDir;
    }

    public boolean hasCompanyBy() {
        return !Ws.isBlank(companyBy);
    }

    public String getCompanyBy() {
        return companyBy;
    }

    public boolean hasDeptBy() {
        return !Ws.isBlank(deptBy);
    }

    public String getDeptBy() {
        return deptBy;
    }

    public int getPvgOwner() {
        return pvgOwner;
    }

    public int getPvgMember() {
        return pvgMember;
    }

    public boolean hasProjectBy() {
        return !Ws.isBlank(projectBy);
    }

    public String getProjectBy() {
        return projectBy;
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

    public boolean hasPriceRetailKey() {
        return !Strings.isBlank(priceRetailKey);
    }

    public String getPriceRetailKey() {
        return priceRetailKey;
    }

    public void setPriceRetailKey(String priceRetailKey) {
        this.priceRetailKey = priceRetailKey;
    }

    public boolean isFeeModeTotal() {
        return OrderFeeMode.TOTAL == this.feeMode;
    }

    public boolean isFeeModeNominal() {
        return OrderFeeMode.NOMINAL == this.feeMode;
    }

    public OrderFeeMode getFeeMode() {
        return feeMode;
    }

    public void setFeeMode(OrderFeeMode feeMode) {
        this.feeMode = feeMode;
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
