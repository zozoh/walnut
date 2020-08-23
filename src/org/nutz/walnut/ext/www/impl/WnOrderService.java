package org.nutz.walnut.ext.www.impl;

import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.WnExecutable;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.thing.WnThingService;
import org.nutz.walnut.ext.www.bean.WnCoupon;
import org.nutz.walnut.ext.www.bean.WnOrder;
import org.nutz.walnut.ext.www.bean.WnOrderStatus;
import org.nutz.walnut.ext.www.bean.WnProduct;
import org.nutz.walnut.ext.www.bean.WnWebSite;
import org.nutz.walnut.util.Wn;

public class WnOrderService {

    private WnIo io;

    private NutMap sellers;

    private WnWebSite site;

    private WnThingService orders;
    private WnThingService coupons;

    public WnOrderService(WnIo io, WnWebSite site) {
        this.io = io;
        this.site = site;
        this.sellers = site.getSellers();
        this.orders = new WnThingService(io, site.getOrderHome());
        if (site.hasCouponHome()) {
            this.coupons = new WnThingService(io, site.getCouponHome());
        }
        if (null == sellers || sellers.isEmpty()) {
            throw Er.create("e.www.order.nil.sellers");
        }
    }

    public WnOrder createOrder(WnOrder or) {
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
        if (Strings.isBlank(or.getPayType())) {
            throw Er.create("e.www.order.nil.pay_tp");
        }

        // 确保订单类型不为空
        String orType = or.getType();
        if (Strings.isBlank(orType)) {
            or.setType("A");
        }
        // 检查订单类型合法性
        else if (!orType.matches("^(A|Q)$")) {
            throw Er.create("e.www.order.invalid.tp", orType);
        }

        // 设置产品的冗余字段
        WnProduct[] pros = or.getProducts();
        String[] proids = new String[pros.length];
        // 第一个商品（因为检查过肯定不为空）
        or.setProductId0(pros[0].getId());
        proids[0] = pros[0].getId();
        // 搞剩下的
        for (int i = 1; i < pros.length; i++) {
            proids[i] = pros[i].getId();
        }
        or.setProductIds(proids);

        // 根据付款类型找到销售方
        String ptPrefix = or.getPayTypePrefix();
        String seller = this.sellers.getString(ptPrefix);
        if (Strings.isBlank(seller)) {
            throw Er.create("e.www.order.invalid.pay_tp", or.getPayType());
        }
        or.setSeller(seller);

        // 设置默认货币单位
        or.setDefaultCurrency(this.site.getCurrency());

        // 依次检查产品列表
        for (WnProduct pro : or.getProducts()) {
            WnObj oPro = io.checkById(pro.getId());
            pro.updateBy(oPro);
        }

        // 检查优惠券列表
        if (or.hasCoupons() && site.hasCouponHome()) {
            for (WnCoupon cpn : or.getCoupons()) {
                WnObj oCpn = coupons.checkThing(cpn.getId(), false);
                cpn.updateBy(oCpn);
            }
        }

        // 计算订单总价
        float totalPrice = 0;
        for (WnProduct pro : or.getProducts()) {
            totalPrice += pro.getPrice() * pro.getAmount();
        }
        or.setPrice(totalPrice);

        // 应用优惠券
        float fee = totalPrice;
        if (or.hasCoupons()) {
            for (WnCoupon cpn : or.getCoupons()) {
                // 不符合条件，无视
                if (!cpn.canUse(totalPrice)) {
                    continue;
                }
                // 叠加应用这张优惠券
                fee = cpn.apply(fee);
            }
        }
        or.setFee(fee);

        // 准备设置其他字段
        or.setStatus(WnOrderStatus.NW);

        // 自动设置标题
        if (Strings.isBlank(or.getTitle())) {
            or.setTitle(or.getSeller());
        }

        // 设置订单过期时间，默认 15 分钟
        long du = 15 * 60000L;
        or.setExpireTime(Wn.now() + du);

        // 准备订单元数据
        NutMap meta = or.toMeta();
        meta.pickAndRemove("id", "ct", "lm");

        // 创建订单对象
        WnObj oOr = orders.createThing(meta);
        or.updateBy(oOr);

        // 返回创建后的订单
        return or;
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
