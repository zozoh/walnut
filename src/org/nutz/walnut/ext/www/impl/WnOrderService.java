package org.nutz.walnut.ext.www.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.lang.Nums;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.WnExecutable;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.lbs.bean.LbsFreight;
import org.nutz.walnut.ext.lbs.bean.LbsFreightRule;
import org.nutz.walnut.ext.lbs.bean.LbsFreightSheet;
import org.nutz.walnut.ext.thing.WnThingService;
import org.nutz.walnut.ext.thing.util.ThQuery;
import org.nutz.walnut.ext.www.bean.OrderPrice;
import org.nutz.walnut.ext.www.bean.PriceRule;
import org.nutz.walnut.ext.www.bean.PriceRuleItem;
import org.nutz.walnut.ext.www.bean.PriceRuleSet;
import org.nutz.walnut.ext.www.bean.WnCoupon;
import org.nutz.walnut.ext.www.bean.WnOrder;
import org.nutz.walnut.ext.www.bean.WnOrderStatus;
import org.nutz.walnut.ext.www.bean.WnProduct;
import org.nutz.walnut.ext.www.bean.WnWebSite;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnPager;

public class WnOrderService {

    private WnIo io;

    private NutMap sellers;

    private WnWebSite site;

    private WnThingService orders;
    private WnThingService coupons;
    private WnThingService addresses;
    private LbsFreightSheet freightSheet;

    private WnObj shipAddrObj;

    private Map<String, PriceRuleSet> priceRules;

    public WnOrderService(WnIo io, WnWebSite site) {
        this.io = io;
        this.site = site;
        this.sellers = site.getSellers();
        this.orders = new WnThingService(io, site.getOrderHome());
        if (site.hasCouponHome()) {
            this.coupons = new WnThingService(io, site.getCouponHome());
        }
        if (site.hasAddressesHome()) {
            this.addresses = new WnThingService(io, site.getAddressesHome());
        }
        if (null == sellers || sellers.isEmpty()) {
            throw Er.create("e.www.order.nil.sellers");
        }
        // 搞个价格规则缓存
        priceRules = new HashMap<>();
    }

    private LbsFreightSheet getFreightSheet() {
        if (null == freightSheet && site.hasFreightSheetObj()) {
            String json = io.readText(site.getFreightSheetObj());
            freightSheet = Json.fromJson(LbsFreightSheet.class, json);
        }
        return freightSheet;
    }

    /**
     * @return 站点默认发货地址对象
     */
    private WnObj getShipAddrObj() {
        if (null == shipAddrObj) {
            if (null != this.addresses) {
                ThQuery tq = new ThQuery();
                tq.qStr = "{dftaddr:true, tp:'S'}";
                tq.wp = new WnPager(1, 0);
                List<WnObj> oAddrs = this.addresses.queryList(tq);
                if (!oAddrs.isEmpty()) {
                    shipAddrObj = oAddrs.get(0);
                }
            }
        }
        return shipAddrObj;
    }

    private PriceRuleSet getRuleSet(String ruleSetId, String ruleKey) {
        PriceRuleSet rs = priceRules.get(ruleSetId);
        if (null == rs) {
            // 还未加载过，加载一个
            if (!priceRules.containsKey(ruleSetId)) {
                WnObj oRuleSet = io.get(ruleSetId);
                if (null == oRuleSet) {
                    priceRules.put(ruleSetId, null);
                }
                // 搞出来看看
                else {
                    List<PriceRule> list = oRuleSet.getAsList(ruleKey, PriceRule.class);
                    if (null == list || list.isEmpty()) {
                        priceRules.put(ruleSetId, null);
                    }
                    // 嗯，建立把
                    else {
                        rs = new PriceRuleSet();
                        rs.addRules(list);
                        priceRules.put(ruleSetId, rs);
                    }
                }
            }
        }
        return rs;
    }

    /**
     * 预计算订单价格
     * 
     * @param or
     *            订单数据（只需要产品列表/收货地址/优惠券部分）
     * @param priceRuleKey
     *            价格规则列表在价格规则对象的键
     * @param user
     *            当前用户（纯计算价格时可以为 null, 就不过滤优惠券了）
     * @return 订单价格详情
     */
    public OrderPrice calculatePrice(WnOrder or, String priceRuleKey, WnAccount user) {
        // 木有商品
        if (!or.hasProducts()) {
            return null;
        }

        // 准备返回值
        OrderPrice orpri = new OrderPrice();
        orpri.setCurrency(or.getCurrency(), site.getCurrency());

        // 依次检查产品列表
        NutMap pcounts = new NutMap(); // 准备汇总各个价格体系下商品数量
        List<WnProduct> avapros = new ArrayList<>(or.getProductsCount());
        for (WnProduct pro : or.getProducts()) {
            // 数量
            int amo = pro.getAmount();
            if (amo < 0) {
                continue;
            }
            // 重新读取价格运费等信息
            WnObj oPro = io.checkById(pro.getId());
            pro.updateBy(oPro);
            pro.setObj(oPro);
            // 记入
            avapros.add(pro);
            // 汇总
            if (pro.hasProId() && pro.hasPriceBy()) {
                String key = pro.getPcountKey();
                int count = pcounts.getInt(key, 0);
                count += amo;
                pcounts.put(key, count);
            }
        }
        or.setProducts(avapros.toArray(new WnProduct[avapros.size()]));

        // !木有商品!
        if (!or.hasProducts()) {
            return null;
        }

        //
        // 计算产品价格
        //
        float proWeight = 0; // 商品记录运费的总重
        float fixFreight = 0; // 商品总固定运费
        float total = 0; // 商品总金额
        float nominal = 0; // 商品标称总价格
        for (WnProduct pro : or.getProducts()) {
            // 得到价格汇总数量
            String key = pro.getPcountKey();
            int pcount = pcounts.getInt(key, 0);
            pro.setPcount(pcount);

            // 计算价格
            // 如果产品设置了复杂的价格规则，譬如根据购买数量决定其价格，需要进行一下
            // 稍微费点劲的计算
            float price = __cal_pro_price(pro, priceRuleKey);
            float retail = pro.getRetail();

            // 数量
            int amo = pro.getAmount();

            // 固定运费
            if (pro.hasFreight()) {
                fixFreight += pro.getFreight() * amo;
            }
            // 要记录重量
            if (pro.hasWeight()) {
                proWeight += pro.getWeight() * amo;
            }

            // 记入
            pro.setPrice(price);
            pro.setSubtotal(price * amo);
            pro.setSubretail(retail * amo);

            // 汇总
            total += pro.getSubtotal();
            nominal += pro.getSubretail();
        }
        orpri.setProducts(or.getProducts());

        //
        // 决定基础价格
        //
        float prefee = site.isFeeModeNominal() ? nominal : total;

        //
        // 本单需要计算运费，并且站点可以计算运费
        //
        float freight = fixFreight;
        if (__cal_load_addr(or)) {
            LbsFreightSheet fs = this.getFreightSheet();
            String country = or.getAddrShipCountry();
            String addrFrom = or.getAddrShipCode();
            String addrTo = or.getAddrUserCode();
            LbsFreightRule rule = fs.findRule(country, addrFrom, addrTo);
            // 找到运费规则就计算一下
            if (null != rule) {
                LbsFreight fr = fs.calculatePrice(rule, proWeight);
                if (null != fr) {
                    freight += fr.getTotal();
                    orpri.setFreightDetail(fr);
                }
            }
        }

        //
        // 应用优惠券
        //
        float fee = prefee;
        if (or.hasCoupons() && site.hasCouponHome()) {
            List<WnCoupon> cpns = new ArrayList<>(or.getCouponsCount());
            for (WnCoupon cpn : or.getCoupons()) {
                // 重新加载优惠券
                WnObj oCpn = coupons.checkThing(cpn.getId(), false);
                if (null == oCpn) {
                    continue;
                }
                cpn.updateBy(oCpn);
                // 不符合条件，无视
                if (cpn.isExpired() || !cpn.isMine(user) || !cpn.canUse(fee)) {
                    continue;
                }
                // 叠加应用这张优惠券
                fee = cpn.apply(fee);
                // 记入优惠券
                cpns.add(cpn);
            }
            or.setCoupons(cpns.toArray(new WnCoupon[cpns.size()]));
        }
        float discount = prefee - fee;

        //
        // 得到总折扣信息以及支付信息
        //

        // 运费对齐到（元）
        freight = Math.round(freight);

        // 其他价格对齐精度到（分）
        total = Nums.precision(total, 2);
        nominal = Nums.precision(nominal, 2);
        prefee = Nums.precision(prefee, 2);
        discount = Nums.precision(discount, 2);

        // 计算订单其他价格相关字段
        float profit = nominal - total;
        float orderPrice = prefee + freight;
        fee = orderPrice - discount;

        //
        // 更新订单
        //
        or.setFreight(freight);
        or.setTotal(total);
        or.setNominal(nominal);
        or.setProfit(profit);
        or.setPrefee(prefee);
        or.setDiscount(discount);
        or.setPrice(orderPrice);
        or.setFee(fee);

        //
        // 更新价格
        //
        orpri.setFreight(freight);
        orpri.setTotal(total);
        orpri.setNominal(nominal);
        orpri.setProfit(profit);
        orpri.setPrefee(prefee);
        orpri.setDiscount(discount);
        orpri.setPrice(orderPrice);
        orpri.setFee(fee);

        // 搞定
        return orpri;
    }

    private float __cal_pro_price(WnProduct pro, String priceRuleKey) {
        if (pro.hasProId() && pro.hasPriceBy()) {
            PriceRuleSet rs = this.getRuleSet(pro.getProId(), priceRuleKey);
            if (null == rs) {
                throw Er.create("e.www.order.price.LostProId", pro.getProId());
            }
            PriceRule rule = rs.getRule(pro.getPriceBy());
            if (null == rule) {
                throw Er.create("e.www.order.price.LostRule",
                                pro.getProId() + ":" + pro.getPriceBy());
            }
            PriceRuleItem ruleItem = rule.matchItem(pro);
            if (null == ruleItem) {
                throw Er.create("e.www.order.price.NoMatchedItem",
                                pro.getId() + "(x" + pro.getAmount() + ")");
            }
            return ruleItem.getPrice();
        }
        return pro.getPrice();
    }

    private boolean __cal_load_addr(WnOrder or) {
        // 订单有了地址
        if (!or.hasAddrUser(false))
            return false;

        // 看看站点是否设置了运费表
        LbsFreightSheet fs = this.getFreightSheet();
        if (null == fs)
            return false;

        // 首先加载站点的默认发货地址
        WnObj shipAo = this.getShipAddrObj();
        if (null == shipAo)
            return false;

        // 读取发货地址
        or.setAddrShipCode(shipAo.getString("code"));
        or.setAddrShipCountry(shipAo.getString("country"));
        or.setAddrShipDoor(shipAo.getString("door"));

        // 发货地址设置不正确，无视
        if (!or.hasAddrShip(false)) {
            return false;
        }

        // 那么即有发货地址又有收货地址咯
        return true;
    }

    public WnOrder createOrder(WnOrder or, String priceRuleKey, WnAccount buyer, String skuKey) {
        // 防守检查：没产品不行啊
        if (!or.hasProducts()) {
            throw Er.create("e.www.order.nil.products");
        }
        // 防守检查：一些关键信息
        if (Strings.isBlank(or.getAccounts())) {
            throw Er.create("e.www.order.nil.accounts");
        }
        if (Strings.isBlank(or.getBuyerId())) {
            throw Er.create("e.www.order.nil.buyer_id");
        }

        // 设置默认货币单位
        or.setDefaultCurrency(this.site.getCurrency());

        // 确保订单类型不为空
        String orType = or.getType();
        if (Strings.isBlank(orType)) {
            or.setType("A");
        }
        // 检查订单类型合法性
        else if (!orType.matches("^(A|Q)$")) {
            throw Er.create("e.www.order.invalid.tp", orType);
        }

        // 计算订单价格 （同时也会重新加载商品优惠券，并重新计算运费）
        if (null == this.calculatePrice(or, priceRuleKey, buyer)) {
            throw Er.create("e.www.order.nil_check.products");
        }

        // 检查订单库存，防止超卖
        WnProduct[] pros = or.getProducts();
        int count = 0;
        if (null != skuKey && or.isTypeA()) {
            for (WnProduct pro : pros) {
                WnObj oPro = pro.getObj();
                int amo = pro.getAmount();
                // 零购买
                if (amo <= 0)
                    continue;
                count += amo;
                // 无视库存
                if (!oPro.has(skuKey))
                    continue;
                int sku = oPro.getInt(skuKey, 0);
                if (amo > sku) {
                    throw Er.create("e.www.order.OutOfStore", pro.getTitle());
                }
            }
        }

        // 根据付款类型找到销售方
        if (or.hasPayType()) {
            checkPayTypeAndSyncSeller(or);
        }

        // 设置产品的冗余字段
        String[] proids = new String[pros.length];

        // 第一个商品（因为检查过肯定不为空）
        or.setProductId0(pros[0].getId());
        proids[0] = pros[0].getId();
        // 搞剩下的
        for (int i = 1; i < pros.length; i++) {
            proids[i] = pros[i].getId();
        }
        or.setProductIds(proids);
        or.setProductCount(count);

        // 准备设置其他字段
        or.setStatus(WnOrderStatus.NW);

        // 自动设置标题
        if (Strings.isBlank(or.getTitle())) {
            or.setTitle(or.getSeller());
        }

        // 设置订单过期时间
        int duInMin = site.getOrderDuMin();
        if (duInMin > 0) {
            long duInMs = duInMin * 60000L;
            or.setExpireTime(Wn.now() + duInMs);
        }

        // 准备订单元数据
        NutMap meta = or.toMeta();
        meta.pickAndRemove("id", "ct", "lm");

        // 创建订单对象
        WnObj oOr = orders.createThing(meta);
        or.updateBy(oOr);

        // 最后根据订单，依次减去商品的库存
        if (null != skuKey && or.isTypeA()) {
            for (WnProduct pro : pros) {
                // 无数量的商品不管（虽然不太可能走到这个分支，还是防一道吧）
                if (pro.getAmount() <= 0)
                    continue;

                // 无视库存
                WnObj oPro = pro.getObj();
                if (null != oPro && !oPro.has("sku"))
                    continue;

                // 减去商品的库存
                int val = pro.getAmount() * -1;
                io.inc(pro.getId(), skuKey, val, false);
            }
        }

        // 返回创建后的订单
        return or;
    }

    public void checkPayTypeAndSyncSeller(WnOrder or) {
        String ptPrefix = or.getPayTypePrefix();
        if (Strings.isBlank(ptPrefix)) {
            throw Er.create("e.www.order.nil.pay_tp_prefix");
        }
        String seller = this.sellers.getString(ptPrefix);
        if (Strings.isBlank(seller)) {
            throw Er.create("e.www.order.invalid.pay_tp", or.getPayType());
        }
        or.setSeller(seller);
    }

    public WnOrder checkOrder(String id) {
        WnOrder or = this.getOrder(id);
        if (null == or) {
            throw Er.create("e.www.order.noexits");
        }
        return or;
    }

    public WnOrder getOrder(String id) {
        WnObj oOr = orders.getThing(id, false);
        if (null != oOr) {
            WnOrder or = new WnOrder();
            or.updateBy(oOr);
            return or;
        }
        return null;
    }

    public WnOrder updateOrder(String id, NutMap meta, WnExecutable executor) {
        WnObj oOr = orders.updateThing(id, meta, executor, null);
        if (null != oOr) {
            WnOrder or = new WnOrder();
            or.updateBy(oOr);
            return or;
        }
        return null;
    }

}
